
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
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import org.apache.commons.lang.RandomStringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * File Tailer base tests
 */
public class BaseTailerTest {
  private static final int LINE_SIZE = 20;
  private static final int ENTRY_NUMBER = 89;
  private static final String LOG_FILE_SIZE = "1KB";
  private static final int QUEUE_SIZE = 200;
  private static final int WRITING_INTERVAL = 1000;
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
  public void baseReadingLogDirTest() throws ConfigurationLoadingException, InterruptedException {
    FileTailerQueue queue = new FileTailerQueue(1);
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();
    LogTailer tailer = TailerLogUtils.createTailer(queue, flowConfig);
    String filePath = flowConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + flowConfig.getSourceConfiguration().getFileName();
    ch.qos.logback.classic.Logger logger =  TailerLogUtils.getSizeLogger(filePath, LOG_FILE_SIZE);
    RandomStringUtils randomUtils = new RandomStringUtils();
    List<String> logList = new ArrayList<String>(ENTRY_NUMBER);

    for (int i = 0; i < ENTRY_NUMBER; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      logger.debug(currLine);
      logList.add(currLine);
    }
    tailer.startAsync();
    Thread.sleep(3000);
    Thread.currentThread().sleep(SLEEP_TIME);
    for (String str:logList)  {
      Assert.assertEquals(true, queue.take().getEventData().contains(str));
    }
    tailer.stopAsync();
  }


  @Test
  public void fileTimeRotationTest() throws ConfigurationLoadingException, InterruptedException {
    FileTailerQueue queue = new FileTailerQueue(QUEUE_SIZE);
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();
    LogTailer tailer = TailerLogUtils.createTailer(queue, flowConfig);
    String filePath = flowConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + flowConfig.getSourceConfiguration().getFileName();
    ch.qos.logback.classic.Logger logger =  TailerLogUtils.getTimeLogger(filePath);
    RandomStringUtils randomUtils = new RandomStringUtils();
    List<String> logList = new ArrayList<String>(ENTRY_NUMBER);
    tailer.startAsync();
    for (int i = 0; i < ENTRY_NUMBER; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      logger.debug(currLine);
      logList.add(currLine);
      Thread.currentThread().sleep(WRITING_INTERVAL);
    }
    Thread.currentThread().sleep(SLEEP_TIME);
    for (String str:logList) {
      Assert.assertEquals(true, queue.take().getEventData().contains(str));
    }
    tailer.stopAsync();
  }
 }
