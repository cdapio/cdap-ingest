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

import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */

public class RunFromSaveStateTest {
  private static final int LINE_SIZE = 20;
  private static final int ENTRY_WRITE_NUMBER = 400;
  private static final String LOG_FILE_SIZE = "10KB";
  private static final int QUEUE_SIZE = 2000;
  private static final int SLEEP_TIME = 5000;

  @Before
  public void prepare() throws IOException {
    TailerLogUtils.createTestDirIfNeed();
    TailerLogUtils.clearTestDir();
  }
  @After
  public void clean() throws IOException {
    TailerLogUtils.deleteTestDir();
  }

  @Test
  public void runFromSaveStateTest() throws Exception {
    FileTailerQueue queue = new FileTailerQueue(QUEUE_SIZE);
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();
    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(flowConfig.getDaemonDir(), flowConfig.getStateFile());
    FileTailerMetricsProcessor metricsProcessor =
      new FileTailerMetricsProcessor(flowConfig.getDaemonDir(), flowConfig.getStatisticsFile(),
                                     flowConfig.getStatisticsSleepInterval(), flowConfig.getPipeName(),
                                     flowConfig.getSourceConfiguration().getFileName());
    LogTailer tailer = new LogTailer(TailerLogUtils.loadConfig(), queue, stateProcessor, metricsProcessor);
    String filePath = flowConfig.getSourceConfiguration().getWorkDir() + "/"
      + flowConfig.getSourceConfiguration().getFileName();

    List<String> logList = new ArrayList<String>(ENTRY_WRITE_NUMBER);
    List<String> readLogList = new ArrayList<String>(ENTRY_WRITE_NUMBER);
    RandomStringUtils randomUtils = new RandomStringUtils();
    ch.qos.logback.classic.Logger logger = TailerLogUtils.getSizeLogger(filePath, LOG_FILE_SIZE);

    Field queueField = queue.getClass().getDeclaredField("queue");
    queueField.setAccessible(true);
    LinkedBlockingQueue<FileTailerEvent> intQueue = (LinkedBlockingQueue<FileTailerEvent>) queueField.get(queue);

    write_log(ENTRY_WRITE_NUMBER, logger, logList);

    tailer.startWorker();
    Thread.currentThread().sleep(SLEEP_TIME);
    saveState(intQueue, queue, readLogList, stateProcessor);

    tailer.stopWorker();
    saveState(intQueue, queue, readLogList, stateProcessor);

    write_log(ENTRY_WRITE_NUMBER, logger, logList);

    tailer.startWorker();

    Thread.currentThread().sleep(SLEEP_TIME);
    saveState(intQueue, queue, readLogList, stateProcessor);

    tailer.stopWorker();
    saveState(intQueue, queue, readLogList, stateProcessor);
    write_log(ENTRY_WRITE_NUMBER, logger, logList);
    tailer.startWorker();

    Thread.currentThread().sleep(SLEEP_TIME);

    tailer.stopWorker();
    saveState(intQueue, queue, readLogList, stateProcessor);
    for (int i = 0; i < logList.size(); i++) {
      Assert.assertEquals(true, readLogList.get(i).contains(logList.get(i)));
    }

  }

  private void saveState(LinkedBlockingQueue<FileTailerEvent> internalQueue, FileTailerQueue queue,
                         List<String> readLogList, FileTailerStateProcessor stateProcessor)
                         throws InterruptedException, FileTailerStateProcessorException {

    while (internalQueue.size() > 0) {
      FileTailerEvent event = queue.take();
      readLogList.add(event.getEventData());
      stateProcessor.saveState(event.getState());
    }
  }
  private void write_log(int entryNumber, ch.qos.logback.classic.Logger logger, List<String> logList) {
    for (int i = 0; i < entryNumber; i++) {
      RandomStringUtils randomUtils = new RandomStringUtils();
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      logger.debug(currLine);
      logList.add(currLine);
    }
  }
}
