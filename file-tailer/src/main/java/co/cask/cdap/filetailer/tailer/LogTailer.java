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
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerState;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
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
  private long sleepInterval;
  private String logDirectory;
  private String logFileName;
  private static final int DEFAULT_BUFSIZE = 4096;
  private FileTailerQueue queue;
  private byte entrySeparator = '\n';
  private ConfigurationLoader confLoader;
  private FileTailerStateProcessor fileTailerStateProcessor;
  private Thread worker;
  /**
   * Buffer on top of RandomAccessFile.
   */
  private final byte inbuf[];

  public LogTailer(ConfigurationLoader loader, FileTailerQueue queue,
                   FileTailerStateProcessor fileTailerStateProcessor) throws  ConfigurationLoaderException {
    this.queue = queue;
    this.inbuf = new byte[DEFAULT_BUFSIZE];
    this.confLoader = loader;
    this.fileTailerStateProcessor = fileTailerStateProcessor;
    this.sleepInterval = confLoader.getSleepInterval();
    this.logDirectory = confLoader.getWorkDir();
    this.logFileName = confLoader.getFileName();
    this.entrySeparator = confLoader.getRecordSeparator();


  }


  public void run() {
    try {
        checkLogDir(logDirectory);
      } catch (LogDirNotFoundException e) {
        LOG.error("Incorrect path to log directory");
       return;
      }
      FileTailerState fileTailerState  = getSaveStateFromFile();
      if (fileTailerState == null) {
          LOG.info("Fail state do not found. Start reading all directory");
          runWithOutRestore();
      } else {
          runFromSaveState(fileTailerState);
    }

  }
 private FileTailerState getSaveStateFromFile() {
     FileTailerState fileTailerState;
     try {
         fileTailerState = fileTailerStateProcessor.loadState();
     } catch (FileTailerStateProcessorException e) {
         LOG.info("Fail state do not exist. Start reading all directory");
         return null;
     }

     return fileTailerState;

 }

  private void runFromSaveState(FileTailerState fileTailerState) {
      long position = fileTailerState.getPosition();
      long lastModifytime = fileTailerState.getLastModifyTime();
      int hash = fileTailerState.getHash();
      File currentLogFile = getCurrentLogFile(logDirectory, lastModifytime);
      if (currentLogFile == null) {
          return;
      }
      try {
        boolean res = checkLine(currentLogFile, position, hash);
        if (!res) {
            return;
        } else {
            startReadingFromFile(currentLogFile, position);
        }
      } catch (IOException e) {
          return;
      }
 }

    private void startReadingFromFile(File currentLogFile, long position) {
        RandomAccessFile reader;
        try {
            reader = new RandomAccessFile(currentLogFile, RAF_MODE);
            reader.seek(position);
        } catch (IOException e) {
            return;
        }
        int lineHash;
            long modifyTime = currentLogFile.lastModified();
          try {
            while (!Thread.currentThread().isInterrupted()) {
                    String line = readLine(reader, entrySeparator).toString();
                    if (line.length() > 0) {
                        lineHash = line.hashCode();
                        LOG.debug("From log file {} readed entry: {}", currentLogFile, line);
                        position = reader.getFilePointer() - line.length();
                        modifyTime = currentLogFile.lastModified();
                        //TODO: Get charset from properties;
                        queue.put(new FileTailerEvent(new FileTailerState(currentLogFile.toString(),
                                                                          position,
                                                                          lineHash,
                                                                          modifyTime),
                                                                          line,
                                                                          Charset.defaultCharset()));
                    } else {
                        File newLog = getCurrentLogFile(logDirectory, modifyTime);
                        if (newLog == null) {
                            Thread.sleep(sleepInterval);
                        } else {
                            LOG.debug("File {} is reading", newLog);
                                   newLog = getCurrentLogFile(logDirectory, modifyTime);
                                currentLogFile = newLog;
                                closeQuietly(reader);
                                reader = new RandomAccessFile(currentLogFile, RAF_MODE);

                        }
                    }
                }
            } catch (IOException e) {
                 LOG.error("Tailer daemon stopped due to IO exception during reading file");
            } catch (InterruptedException e) {
              LOG.info("Tailer daemon was interrupted");
            } finally {
              closeQuietly(reader);
            }

    }

    private void runWithOutRestore() {
        File logFile = null;
            while (logFile == null && !Thread.currentThread().isInterrupted()) {
                logFile = getCurrentLogFile(logDirectory, (long) 0);
                if (logFile == null) {
                    try {
                        Thread.sleep(sleepInterval);
                    } catch (InterruptedException e) {
                        LOG.info("Tailer daemon was interrupted");
                        break;
                    }
                }
        }
        startReadingFromFile(logFile, 0);
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


  private File getCurrentLogFile(String logDir, Long currentTime) {
    File[] dirFiles = new File(logDir).listFiles(new LogFilter(logFileName));
    if (dirFiles.length == 0) {
      return null;
    }
    TreeMap<Long, File> logFilesTimesMap = new TreeMap<Long, File>();
    for (File f:dirFiles) {
      logFilesTimesMap.put(f.lastModified(), f);
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


  private StringBuilder readLine(RandomAccessFile reader, byte separator) throws IOException {
    StringBuilder sb = new StringBuilder();

    long rePos = reader.getFilePointer();
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
    return sb;
  }

  private void closeQuietly(RandomAccessFile reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
          LOG.warn("Exception during clousing");

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
