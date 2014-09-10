/*
 * Copyright 2014 Cask Data, Inc.
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

import co.cask.cdap.filetailer.AbstractWorker;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerState;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Tailer daemon
 */
public class LogTailer extends AbstractWorker {

  private static final Logger LOG = LoggerFactory.getLogger(LogTailer.class);
  private static final String RAF_MODE = "r";
  private static final int DEFAULT_BUFSIZE = 4096;

  private final long sleepInterval;
  private final File logDirectory;
  private final String logFileName;
  private final String charsetName;
  private Charset charset;
  private final int failureRetryLimit;
  private final long failureSleepInterval;
  private final FileTailerQueue queue;
  private final char entrySeparator;
  private final PipeConfiguration confLoader;
  private final FileTailerStateProcessor fileTailerStateProcessor;
  private final FileTailerMetricsProcessor metricsProcessor;
  private final ByteBuffer buffer;
  private final String rotationPattern;


  public LogTailer(PipeConfiguration loader, FileTailerQueue queue,
                   FileTailerStateProcessor stateProcessor, FileTailerMetricsProcessor metricsProcessor) {
    buffer = ByteBuffer.allocate(DEFAULT_BUFSIZE);
    this.queue = queue;
    this.confLoader = loader;
    this.fileTailerStateProcessor = stateProcessor;
    this.metricsProcessor = metricsProcessor;
    this.sleepInterval = confLoader.getSourceConfiguration().getSleepInterval();
    this.logDirectory = confLoader.getSourceConfiguration().getWorkDir();
    this.logFileName = confLoader.getSourceConfiguration().getFileName();
    this.entrySeparator = confLoader.getSourceConfiguration().getRecordSeparator();
    this.charsetName = confLoader.getSourceConfiguration().getCharsetName();
    this.failureRetryLimit = confLoader.getSourceConfiguration().getFailureRetryLimit();
    this.failureSleepInterval = confLoader.getSourceConfiguration().getFailureSleepInterval();
    this.rotationPattern = confLoader.getSourceConfiguration().getRotationPattern();
  }

  /**
   *  Runs the log tailer thread.
   */
  public void run() {
    try {
      checkLogDirExists(logDirectory);
    } catch (LogDirNotFoundException e) {
      LOG.error("Incorrect path to log directory; directory {} does not exist", logDirectory.getAbsolutePath());
      return;
    }
    if (!charsetSetup()) {
      LOG.error("Charset {} is not supported", charsetName);
      return;
    }
    FileTailerState fileTailerState = getSaveStateFromFile();
    try {
      if (fileTailerState == null) {
        LOG.info("File Tailer state was not found; start reading all logs from the directory from the beginning");
        runWithOutRestore();
      } else {
        runFromSaveState(fileTailerState);
      }
    } catch (InterruptedException e) {
      LOG.info("Tailer daemon was interrupted");
    }
    LOG.info("Tailer daemon stopped");
  }

  /**
   *  Sets the charset using the name specified in the configuration file.
   *
   *  @return false if the charset with specified name does not exist; true if the charset was successfully setup
   */
  private boolean charsetSetup() {
    if (!Charset.isSupported(charsetName)) {
      return false;
    }
    charset = Charset.forName(charsetName);
    return true;
  }

  /**
   *  Retrieves the saved state of the File Tailer.
   *
   *  @return the state; <code>null</code> if the save state file does not exist
   */
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

  /**
   *  Method try start  tailer from save state.
   *  If could not find log file with saved entry method finished
   *
   *  @throws  InterruptedException if thread was interrupted
   */
  private void runFromSaveState(FileTailerState fileTailerState) throws InterruptedException {
    long position = fileTailerState.getPosition();
    long lastModifytime = fileTailerState.getLastModifyTime();
    String savedFilename = fileTailerState.getFileName();
    int hash = fileTailerState.getHash();
    File currentLogFile = getNextLogFile(logDirectory.getAbsolutePath(), lastModifytime,
                                         true, new File(savedFilename));
    if (currentLogFile == null) {
      LOG.info("Saved log file not exist. Exiting");
      return;
    }
    try {
      FileChannel channel = tryOpenFile(currentLogFile);
      if (!checkLine(channel, position, hash)) {
        LOG.error("Can not find line from saved state. Exiting.. ");
        return;
      }
      LOG.info("Saved log entry was found. Start reading log from save state");
      startReadingFromFile(channel, currentLogFile);
    } catch (IOException e) {
      return;
    }
  }

  /**
   *  Starts reading in the log directory using the current log file
   *  and the current RandomAccessReader position.
   *
   *  @param channel opened RandomAccessReader stream
   *  @param currentLogFile log file, from which reading is started
   *  @throws  InterruptedException if thread was interrupted
   */
  private void startReadingFromFile(FileChannel channel, File currentLogFile) throws InterruptedException {
    int lineHash;
    long position;
    long modifyTime = currentLogFile.lastModified();
    try {
      while (isRunning()) {
        String line = tryReadLine(channel, entrySeparator);
        if (line.length() > 0) {
          lineHash = line.hashCode();
          LOG.debug("From log file {} readed entry: {}", currentLogFile, line);
          position = channel.position() - line.length() - 1;
          modifyTime = currentLogFile.lastModified();
          queue.put(new FileTailerEvent(new FileTailerState(currentLogFile.toString(),
                                                            position, lineHash, modifyTime),
                                        line, charset));

          metricsProcessor.onReadEventMetric(line.getBytes().length);
        } else {
          File newLog = getNextLogFile(logDirectory.getAbsolutePath(), modifyTime, false, currentLogFile);
          if (newLog == null) {
            LOG.debug("Waiting for new log data from file {}", currentLogFile);
            Thread.sleep(sleepInterval);
          } else {
            LOG.debug("Reading file {}", newLog);
            currentLogFile = newLog;
            closeQuietly(channel);
            channel  = (new RandomAccessFile(currentLogFile, RAF_MODE)).getChannel();
            modifyTime = currentLogFile.lastModified();
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Tailer daemon stopped due to IO exception while reading file: {}", e.getMessage());
    } finally {
      closeQuietly(channel);
    }
  }

  /**
   *  Method start reading log from all log directory
   *
   *  @InterruptedException if thread was interrupted
   */
  private void runWithOutRestore() throws InterruptedException {
    File logFile = null;
    FileChannel channel;
    while (logFile == null && isRunning()) {
      logFile = getNextLogFile(logDirectory.getAbsolutePath(), 0L, false, new File(logFileName));
      if (logFile == null) {
        try {
          Thread.sleep(sleepInterval);
        } catch (InterruptedException e) {
          LOG.info("Tailer daemon was interrupted");
          return;
        }
      }
    }
    try {
      channel = tryOpenFile(logFile);
    } catch (IOException e) {
      return;
    }
    startReadingFromFile(channel, logFile);
  }

  /**
   *  Method start reading log from all log directory
   *
   *  @InterruptedException if thread was interrupted
   */
  private boolean checkLine(FileChannel channel, long position, int hash) throws IOException, InterruptedException {
    channel.position(position);
    String line = tryReadLine(channel, entrySeparator).toString();
    if (line.length() > 0 && line.hashCode() == hash) {
      return true;
    } else {
      return false;
    }
  }

  /**
   *  Method get next log file if exist
   *
   *  @param logDir current log directory
   *  @param currentTime time of the last current log file modification
   *  @param fromSaveState if starting from save state
   *  @param currFile current log file name
   *  @return  next log file
   */
  private File getNextLogFile(String logDir, Long currentTime, boolean fromSaveState, File currFile) {
    File[] dirFiles = new File(logDir).listFiles(new LogFilter(logFileName, rotationPattern));

    Comparator logfileComparator = new Comparator<LogFileTime>() {
      @Override
      public int compare(LogFileTime o1, LogFileTime o2) {
        int res = o1.getModificationTime().compareTo(o2.getModificationTime());
        if (res != 0) {
          return res;
        } else {
          res = o2.getFileName().length() - o1.getFileName().length();

        }
        return (res != 0) ? res : o2.getFileName().compareTo(o1.getFileName());
      }
    };
    TreeMap<LogFileTime, File> logFilesTimesMap = new TreeMap<LogFileTime, File>(logfileComparator);
    for (File f : dirFiles) {
      logFilesTimesMap.put(new LogFileTime(f.lastModified(), f.getName()), f);
    }
    if (currentTime == 0 && logFilesTimesMap.size() > 0) {
      return logFilesTimesMap.firstEntry().getValue();
    }
    if (fromSaveState && logFilesTimesMap.containsKey(new LogFileTime(currentTime, currFile.getName()))) {
      return logFilesTimesMap.get(new LogFileTime(currentTime, currFile.getName()));
    }
    boolean currentFileChanged = true;
    for (LogFileTime logFileTime : logFilesTimesMap.keySet()) {
      if (logFileTime.getModificationTime().equals(currentTime)) {
        currentFileChanged = false;
        break;
      }
    }
    if (currentFileChanged && logFilesTimesMap.higherKey(new LogFileTime(currentTime, currFile.getName())) == null) {
      return null;
    }
    LogFileTime key = logFilesTimesMap.higherKey(new LogFileTime(currentTime, currFile.getName()));

    return key == null ? null : logFilesTimesMap.get(key);
  }

  class LogFileTime {
    private final long modificationTime;
    private final String fileName;

    LogFileTime(long modificationTime, String fileName) {
      this.modificationTime = modificationTime;
      this.fileName = fileName;
    }

    public Long getModificationTime() {
      return modificationTime;
    }

    public String getFileName() {
      return fileName;
    }
  }

  /**
   *  Method get next log file if exist
   *
   *  @throws LogDirNotFoundException if directory specified in log file not exist
   */
  private void checkLogDirExists(File dir) throws LogDirNotFoundException {
    if (!(dir.exists())) {
      throw new LogDirNotFoundException("Configured log directory not found");
    }
  }

  /**
   *  Try open for reading  log file
   *
   *  @param file log file
   *  @throws IOException if could not open reader after failureRetryLimit attempts
   *  @throws InterruptedException if thread was interrupted
   */
  private FileChannel tryOpenFile(File file) throws IOException, InterruptedException {
    int retryNumber = 0;
    RandomAccessFile reader = null;
    FileChannel channel = null;
    while (isRunning()) {
      if (retryNumber > failureRetryLimit && failureRetryLimit > 0) {
        LOG.error("fail to open file after {} attempts", retryNumber);
        throw new IOException();
      }
      try {
        reader = new RandomAccessFile(file, RAF_MODE);
        channel = reader.getChannel();
        break;
      } catch (IOException e) {
        retryNumber++;
        Thread.sleep(failureSleepInterval);
      }
    }
    return channel;
  }

  /**
   *  Method try read log entry from log file
   *
   *  @param channel FileChannel steam
   *  @param separator  log entry separator
   *  @throws IOException in case could not read entry after failureRetryLimit attempts
   *  @throws InterruptedException in case thread was interrupted
   */
  private String tryReadLine(FileChannel channel, char separator) throws IOException, InterruptedException {
    int retryNumber = 0;
    long rePos = 0;
    StringBuilder sb = new StringBuilder();
    while (isRunning()) {
      if (retryNumber > failureRetryLimit && failureRetryLimit > 0) {
        LOG.error("fail to read line  after {} attempts", retryNumber);
        throw new IOException();
      }
      try {
        rePos = channel.position();
        buffer.clear();
        boolean end = false;
        int num;
        while (((num = channel.read(buffer)) != -1) && !end) {
          buffer.flip();
          CharBuffer charBuffer = charset.decode(buffer);
          for (int i = 0; i < num; i++) {
            char ch = charBuffer.charAt(i);

            if (ch != separator) {
              sb.append(ch);
              rePos += ((Character) ch).toString().getBytes(charset).length;
            } else {
              rePos += ((Character) separator).toString().getBytes(charset).length;
              end = true;
              break;
            }
          }
          buffer.clear();
        }
        break;
      } catch (IOException e) {
        retryNumber++;
        Thread.sleep(failureSleepInterval);
      }
    }
    channel.position(rePos);
    return sb.toString();
  }

  private void closeQuietly(FileChannel channel) {
    if (channel != null) {
      try {
        channel.close();
      } catch (IOException e) {
        LOG.warn("Exception during closing: {}", e.getMessage(), e);
      }
    }
  }
}
