/*
 * Copyright 2014 Cask, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.filetailer.tailer;

/**
 * Created by yura on 15.08.14.
 */

import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.TreeMap;

/**
 * Tailer daemon
 */

public class LogTailer implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(LogTailer.class);
  private static final String RAF_MODE = "r";
  private static long sleepInterval;
  private static String logDirectory;
  private static String logFileName;
  private static final int DEFAULT_BUFSIZE = 4096;
  private final FileTailerQueue queue;
  private static byte entrySeparator = '\n';
  private static ConfigurationLoader confLoader;
  private Thread worker;
  /**
   * Buffer on top of RandomAccessFile.
   */
  private final byte inbuf[];

  public LogTailer(ConfigurationLoader loader, FileTailerQueue queue) {
    this.queue = queue;
    this.inbuf = new byte[DEFAULT_BUFSIZE];
    confLoader = loader;


  }

  private void loadConfig() throws ConfigurationLoaderException {
    sleepInterval = confLoader.getSleepInterval();
    logDirectory = confLoader.getWorkDir();
    logFileName = confLoader.getFileName();


    //this.entrySeparator = confLoader.loadRecordSeparator();

  }


  public void run() {
    try {
      this.loadConfig();
    } catch (ConfigurationLoaderException e) {
      LOG.error("Error during confiiguration loading");
      return;
    }
    while (!Thread.currentThread().isInterrupted()) {
      try {
        checkLogDir(logDirectory);
      } catch (LogDirNotFoundException e) {
        LOG.error("Incorrect path to log directory");
        break;
      }
      checkIfRestoreFileExist();
      try {
        runWithOutRestore();
      } catch (InterruptedException e) {
        break;
      }

    }

  }

  private void runWithOutRestore() throws InterruptedException {
    RandomAccessFile reader = null;
    File logFile = null;
    try {
      while (logFile == null) {

        logFile = getCurrentLogFile(logDirectory, (long) 0);
        if (logFile == null) {
          Thread.sleep(sleepInterval);
        }

      }
      LOG.debug("File {} is reading", logFile);

      reader = new RandomAccessFile(logFile, RAF_MODE);
      int lineHash = 0;
      long modifyTime = logFile.lastModified();
      while (true) {

        String line = readLine(reader, entrySeparator).toString();
        long position = 0;
        if (line.length() > 0) {
          lineHash = line.hashCode();
          LOG.debug("From log file {} readed entry: {}", logFile, line);
          position = reader.getFilePointer() - line.length();
          modifyTime = logFile.lastModified();
          //TODO: Get charset from properties;
          queue.put(new FileTailerEvent(new FileTailerState(logFile.toString(), position, lineHash, modifyTime), line, Charset.defaultCharset()));
        } else {
          File newLog = getCurrentLogFile(logDirectory, modifyTime);
          if (newLog == null) {
            Thread.sleep(sleepInterval);
          } else {
            LOG.debug("File {} is reading", newLog);

            if (!checkLine(newLog, position, lineHash)) {
              newLog = getCurrentLogFile(logDirectory, modifyTime);
              logFile = newLog;
              closeQuietly(reader);
              reader = new RandomAccessFile(logFile, RAF_MODE);
            } else {
              LOG.debug("Rotation detected");

            }


          }
        }
      }
    } catch (IOException e) {
      throw new InterruptedException();

    }
  }

  private boolean checkLine(File newFile, long position, int hash) throws IOException {
    RandomAccessFile reader = new RandomAccessFile(newFile, RAF_MODE);
    reader.seek(position);
    String line = readLine(reader, entrySeparator).toString();
    closeQuietly(reader);
    if (line.length() > 0 && line.hashCode() == hash) {
      return true;
    } else {
      return false;
    }

  }

  private void checkIfRestoreFileExist() {
  }

  /**
   * Allows the tailer to complete its current loop and return.
   */
  private File getCurrentLogFile(String logDir, Long currentTime) {
    File[] dirFiles = new File(logDir).listFiles(new LogFilter(logFileName));
    if (dirFiles.length == 0) {
      return null;
    }
    TreeMap<Long, File> logFilesTimesMap = new TreeMap<Long, File>();
    for (int i = 0; i < dirFiles.length; i++) {
      logFilesTimesMap.put(dirFiles[i].lastModified(), dirFiles[i]);
    }
    if (currentTime == 0) {
      return logFilesTimesMap.firstEntry().getValue();
    }
    Long key = logFilesTimesMap.higherKey(currentTime);
    if (key == null) {
      return null;
    } else {
      return logFilesTimesMap.get(key);
    }
  }

  private void checkLogDir(String dir) throws LogDirNotFoundException {
    File logdir = new File(dir);
    if (!(logdir.exists())) {
      throw new LogDirNotFoundException("Configured log directory not found");
    }
  }


  /**
   * Read new lines.
   *
   * @param reader The file to read
   * @return The new position after the lines have been read
   * @throws java.io.IOException if an I/O error occurs.
   */
  private StringBuilder readLine(RandomAccessFile reader, byte separator) throws IOException {
    StringBuilder sb = new StringBuilder();

    long pos = reader.getFilePointer();
    long rePos = pos; // position to re-read

    int num;
    while (((num = reader.read(inbuf)) != -1)) {
      for (int i = 0; i < num; i++) {
        byte ch = inbuf[i];
        if (ch != separator) {
          sb.append((char) ch);
          rePos++;
        } else {
          sb.append((char) ch);
          rePos++;
          break;
        }
      }
    }
    reader.seek(rePos);

    return sb;
  }

  private void closeQuietly(RandomAccessFile reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {

      }
    }


  }

  public void start() {
    if (worker == null) {
      worker = new Thread(this);
      worker.start();
    } else {
      LOG.warn("Tailer deamon is already started!");
      throw new IllegalStateException("Tailer deamon is already started!");
    }
  }

  public void stop() {
    if (worker != null) {
      worker.interrupt();
    } else {
      LOG.warn("Tailer deamon is not started!");
      throw new IllegalStateException("Tailer deamon is not started!");

    }
  }

}
