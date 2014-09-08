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
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File Tailer test for size based rotation logs
 */
public class SizeBasedRotationTest {
  private static final int LINE_SIZE = 20;
  private static final int ENTRY_WRITE_NUMBER = 7000;
  private static final String LOG_FILE_SIZE = "50KB";
  private static final int QUEUE_SIZE = 9000;

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
  public void fileRotationTest() throws ConfigurationLoadingException, InterruptedException {
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
    //  tailer.startWorker();
    for (int i = 0; i < ENTRY_WRITE_NUMBER; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      logger.debug(currLine);
      logList.add(currLine);
      if (i % 100 == 0) {
         Thread.sleep(100);
      }
    }
    tailer.startAsync();
    Thread.sleep(1000);
    for (int i = 0; i < logList.size(); i++) {
      Assert.assertEquals(true, queue.take().getEventData().contains(logList.get(i)));
    }

    tailer.stopAsync();
  }
}
