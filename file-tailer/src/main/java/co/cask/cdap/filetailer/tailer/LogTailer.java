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
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Tailer daemon
 */

public class LogTailer extends AbstractWorker {

  private static final Logger LOG = LoggerFactory.getLogger(LogTailer.class);
  private static final String RAF_MODE = "r";
  private long sleepInterval;
  private String logDirectory;
  private String logFileName;
  private String charsetName;
  private Charset charset;
  private int failureRetryLimit;
  private long failureSleepInterval;
  private static final int DEFAULT_BUFSIZE = 4096;
  private FileTailerQueue queue;
  private byte entrySeparator = '\n';
  private PipeConfiguration confLoader;
  private FileTailerStateProcessor fileTailerStateProcessor;
  private FileTailerMetricsProcessor metricsProcessor;
  private final byte inbuf[];
  private String rotationPattern;


  public LogTailer(PipeConfiguration loader, FileTailerQueue queue,
                   FileTailerStateProcessor stateProcessor, FileTailerMetricsProcessor metricsProcessor) {
    inbuf = new byte[DEFAULT_BUFSIZE];
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

  public FileTailerMetricsProcessor getMetricsProcessor() {
    return metricsProcessor;
  }

  /**
   *  run log tailer thread
   */
  public void run() {
    try {
      checkLogDir(logDirectory);
    } catch (LogDirNotFoundException e) {
      LOG.error("Incorrect path to log directory. Directory: {} not exist", logDirectory);
      return;
    }
    if (!charsetSetup()) {
      LOG.error("Charset: {} is not supported", charsetName);
      return;
    }
    FileTailerState fileTailerState = getSaveStateFromFile();
    try {
      if (fileTailerState == null) {
        LOG.info("Fail state do not found. Start reading all directory");
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
   *  setup charset from name, specified in configuration
   *  @return false if charset with given name does not exist, true if charset was setup  successfully
   */
  private boolean charsetSetup() {
    if (!Charset.isSupported(charsetName)) {
      return false;
    }
    charset = Charset.forName(charsetName);
    return true;
  }
  /**
   *  Method try to get save state of the tailer
   *  @return FileTailerState object, if save state file not exist null is returning
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
   *  @throws  InterruptedException if thread was interrupted
   */
  private void runFromSaveState(FileTailerState fileTailerState) throws InterruptedException {
    long position = fileTailerState.getPosition();
    long lastModifytime = fileTailerState.getLastModifyTime();
    int hash = fileTailerState.getHash();
    File currentLogFile = getNextLogFile(logDirectory, lastModifytime, true, null);
    if (currentLogFile == null) {
      return;
    }
    try {
      RandomAccessFile reader = tryOpenFile(currentLogFile);
      if (!checkLine(reader, position, hash)) {
        LOG.error("Can not find line from saved state. Exiting.. ");
        return;
      } else {
        startReadingFromFile(reader, currentLogFile);
      }
    } catch (IOException e) {
      return;
    }
  }
  /**
   *  Method start reading log directory from current log file and
   *  current RandomAccessReader position
   *  @param reader opened RandomAccessReader stream
   *  @param currentLogFile log file, from which reading is started
   *  @throws  InterruptedException if thread was interrupted
   */
  private void startReadingFromFile(RandomAccessFile reader, File currentLogFile) throws InterruptedException {
    int lineHash;
    long position;
    long modifyTime = currentLogFile.lastModified();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        String line = tryReadLine(reader, entrySeparator);
        if (line.length() > 0) {
          lineHash = line.hashCode();
          LOG.debug("From log file {} readed entry: {}", currentLogFile, line);
          position = reader.getFilePointer() - line.length() - 1;
          modifyTime = currentLogFile.lastModified();
          queue.put(new FileTailerEvent(new FileTailerState(currentLogFile.toString(),
                                                            position, lineHash, modifyTime),
                                        line, charset));

          metricsProcessor.onReadEventMetric(line.getBytes().length);
        } else {
          if (!confLoader.getSourceConfiguration().getReadRotatedFiles()) {
//            TODO: file alredy read
          }
          File newLog = getNextLogFile(logDirectory, modifyTime, false, currentLogFile);
          if (newLog == null) {
            LOG.debug("waiting for new log data  from file {}", currentLogFile);
            Thread.sleep(sleepInterval);
          } else {
            LOG.debug("File {} is reading", newLog);
            currentLogFile = newLog;
            closeQuietly(reader);
            reader = new RandomAccessFile(currentLogFile, RAF_MODE);
            modifyTime = currentLogFile.lastModified();
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Tailer daemon stopped due to IO exception during reading file");
    } finally {
      closeQuietly(reader);
    }

  }
  /**
   *  Method start reading log from all log directory
   *  @InterruptedException if thread was interrupted
   */
  private void runWithOutRestore() throws InterruptedException {
    File logFile = null;
    RandomAccessFile reader;
    while (logFile == null && !Thread.currentThread().isInterrupted()) {
      logFile = getNextLogFile(logDirectory, 0L, false, null);
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
      reader = tryOpenFile(logFile);
    } catch (IOException e) {
      return;
    }
    startReadingFromFile(reader, logFile);
  }

  /**
   *  Method start reading log from all log directory
   *  @InterruptedException if thread was interrupted
   */
  private boolean checkLine(RandomAccessFile reader, long position, int hash) throws IOException, InterruptedException {
    reader.seek(position);
    String line = tryReadLine(reader, entrySeparator).toString();
    if (line.length() > 0 && line.hashCode() == hash) {
      return true;
    } else {
      return false;
    }

  }
  /**
   *  Method get next log file if exist
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
    if (currentTime == 0) {
      return logFilesTimesMap.firstEntry().getValue();
    }
    if (fromSaveState && logFilesTimesMap.containsKey(new LogFileTime(currentTime, null))) {
      return logFilesTimesMap.get(new LogFileTime(currentTime, null));
    } else {
      LogFileTime key = logFilesTimesMap.higherKey(new LogFileTime(currentTime, currFile.getName()));
      if (key == null) {
        return null;
      } else {
        return logFilesTimesMap.get(key);
      }
    }
  }

  class LogFileTime {
    private Long modificationTime;
    private String fileName;

    LogFileTime(Long modificationTime, String fileName) {
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
   *  @throws co.cask.cdap.filetailer.tailer.LogDirNotFoundException if directory specified in log file not exist
   */
  private void checkLogDir(String dir) throws LogDirNotFoundException {
    File logdir = new File(dir);
    if (!(logdir.exists())) {
      throw new LogDirNotFoundException("Configured log directory not found");
    }
  }
  /**
   *  try open for reading  log file
   *  @param file log file
   *  @throws IOException if could not open reader after failureRetryLimit attempts
   *  @throws InterruptedException if thread was interrupted
   */
  private RandomAccessFile tryOpenFile(File file) throws IOException, InterruptedException {
    int retryNumber = 0;
    RandomAccessFile reader;
    while (true) {
      if (retryNumber > failureRetryLimit && failureRetryLimit > 0) {
        LOG.error("fail to open file after {} attempts", retryNumber);
        throw new IOException();
      }
      try {
        reader = new RandomAccessFile(file, RAF_MODE);
        break;
      } catch (IOException e) {
        retryNumber++;
        Thread.sleep(failureSleepInterval);
      }
    }
    return reader;
  }
  /**
   *  Method try read  log entry from log file
   *  @param reader  RandomAccessReader steam
   *  @param separator  log entry separator
   *  @throws IOException if could not read entry after failureRetryLimit attempts
   *  @throws InterruptedException if thread was interrupted
   */
  private String tryReadLine(RandomAccessFile reader, byte separator) throws IOException, InterruptedException {
    int retryNumber = 0;
    long rePos = 0;

    StringBuilder sb = new StringBuilder();
    while (retryNumber < failureRetryLimit || failureRetryLimit == 0) {
      try {
        rePos = reader.getFilePointer();
        boolean end = false;
        int num;
        while (((num = reader.read(inbuf)) != -1) && !end) {
          for (int i = 0; i < num; i++) {
            byte ch = inbuf[i];
            if (ch != separator) {
              sb.append((char) ch);
              rePos++;
            } else {
              rePos++;
              end = true;
              break;
            }
          }
        }
        break;
      } catch (IOException e) {
        retryNumber++;
        Thread.sleep(failureSleepInterval);
      }
    }
    if (retryNumber >= failureRetryLimit && failureRetryLimit > 0) {
      LOG.error("fail to read line  after {} attempts", retryNumber);
      throw new IOException();
    }
    reader.seek(rePos);
    return sb.toString();
  }

  private void closeQuietly(RandomAccessFile reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        LOG.warn("Exception during closing", e.getMessage());

      }
    }


  }

}
