
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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
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
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class TailerTest {
  private static final int LINE_SIZE = 20;
  private static final int ENTRY_NUMBER = 121;
  private static final String LOG_FILE_SIZE = "1KB";
  private static final int QUEUE_SIZE = 200;
  private static final int WRITING_INTERVAL = 1000;

  @Before
  public void prepare() throws IOException {
    TailerLogUtils.createTestDirIfNeed();
    TailerLogUtils.clearTestDir();
//    TailerLogUtils.clearStateDir();
  }
@After
public void clean() throws IOException {
    TailerLogUtils.deleteTestDir();
  }

  @Test
  public void baseReadingLogDirTest() throws ConfigurationLoadingException, InterruptedException {
    FileTailerQueue queue = new FileTailerQueue(1);
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();

    String filePath = flowConfig.getSourceConfiguration().getWorkDir() + "/"
      + flowConfig.getSourceConfiguration().getFileName();

    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(flowConfig.getDaemonDir(), flowConfig.getStateFile());
    FileTailerMetricsProcessor metricsProcessor =
        new FileTailerMetricsProcessor(flowConfig.getDaemonDir(), flowConfig.getStatisticsFile(),
                                       flowConfig.getStatisticsSleepInterval(), flowConfig.getPipeName(),
                                       flowConfig.getSourceConfiguration().getFileName());

    LogTailer tailer = new LogTailer(TailerLogUtils.loadConfig(), queue, stateProcessor, metricsProcessor);
    ch.qos.logback.classic.Logger logger =  getSizeLogger(filePath, LOG_FILE_SIZE);
    RandomStringUtils randomUtils = new RandomStringUtils();
    List<String> logList = new ArrayList<String>(ENTRY_NUMBER);

    for (int i = 0; i < ENTRY_NUMBER; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
    logger.debug(currLine);
    logList.add(currLine);
    }
    tailer.startWorker();
    Thread.sleep(3000);
    for (String str:logList)  {
    Assert.assertEquals(true, queue.take().getEventData().contains(str));
    }
    tailer.stopWorker();
  }


  @Test
  public void fileTimeRotationTest() throws ConfigurationLoadingException, InterruptedException {

    FileTailerQueue queue = new FileTailerQueue(QUEUE_SIZE);
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();

    String filePath = flowConfig.getSourceConfiguration().getWorkDir() + "/"
      + flowConfig.getSourceConfiguration().getFileName();
    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(flowConfig.getDaemonDir(), flowConfig.getStateFile());

    FileTailerMetricsProcessor metricsProcessor =
      new FileTailerMetricsProcessor(flowConfig.getDaemonDir(), flowConfig.getStatisticsFile(),
                                     flowConfig.getStatisticsSleepInterval(), flowConfig.getPipeName(),
                                     flowConfig.getSourceConfiguration().getFileName());

    LogTailer tailer = new LogTailer(TailerLogUtils.loadConfig(), queue, stateProcessor, metricsProcessor);
    ch.qos.logback.classic.Logger logger =  getTimeLogger(filePath);
    RandomStringUtils randomUtils = new RandomStringUtils();
    List<String> logList = new ArrayList<String>(ENTRY_NUMBER);
    List<String> queueReturnList = new ArrayList<String>(ENTRY_NUMBER);
      tailer.startWorker();
    for (int i = 0; i < ENTRY_NUMBER; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
         logger.debug(currLine);
      logList.add(currLine);

         Thread.sleep(WRITING_INTERVAL);
    }
    Thread.sleep(2000);
    for (String str:logList) {
      Assert.assertEquals(true, queue.take().getEventData().contains(str));
    }

    tailer.stopWorker();
  }



  private  ch.qos.logback.classic.Logger getSizeLogger(String file, String fileSize) {

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    ch.qos.logback.core.rolling.RollingFileAppender fileAppender =
      new ch.qos.logback.core.rolling.RollingFileAppender();
    fileAppender.setContext(loggerContext);
    fileAppender.setFile(file);
    fileAppender.setAppend(true);
    FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setFileNamePattern("new.log" + "%i");
    rollingPolicy.setParent(fileAppender);
    rollingPolicy.start();
    fileAppender.setRollingPolicy(rollingPolicy);
    SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
    triggeringPolicy.setContext(loggerContext);
    triggeringPolicy.setMaxFileSize(fileSize);
    triggeringPolicy.start();
    fileAppender.setTriggeringPolicy(triggeringPolicy);
    PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
    layoutEncoder.setContext(loggerContext);
    layoutEncoder.setPattern("[%d  %-5p %c{1}] %msg%n");
    layoutEncoder.start();
    fileAppender.setEncoder(layoutEncoder);
    fileAppender.start();

    // configures  logger
    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(TailerTest.class.getName() + "size");
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    rootLogger.addAppender(fileAppender);
    return  rootLogger;

  }

  private  ch.qos.logback.classic.Logger getTimeLogger(String file) {

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    ch.qos.logback.core.rolling.RollingFileAppender fileAppender =
      new ch.qos.logback.core.rolling.RollingFileAppender();
    fileAppender.setContext(loggerContext);
    fileAppender.setFile(file);
    fileAppender.setAppend(true);
    TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setParent(fileAppender);
    rollingPolicy.setFileNamePattern(file + "%d{yyyy-MM-dd_HH-mm}");
    rollingPolicy.start();
    fileAppender.setRollingPolicy(rollingPolicy);
    PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
    layoutEncoder.setContext(loggerContext);
    layoutEncoder.setPattern("[%d  %-5p %c{1}] %msg%n");
    layoutEncoder.start();
    fileAppender.setEncoder(layoutEncoder);
    fileAppender.start();


    // configures the root logger
    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(TailerTest.class.getName() + "time");
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    rootLogger.addAppender(fileAppender);
    return  rootLogger;

  }
}
