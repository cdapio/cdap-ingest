/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.filetailer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.filetailer.Pipe;
import co.cask.cdap.filetailer.PipeManager;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.tailer.LogTailer;
import com.google.common.util.concurrent.ServiceManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.lang.System;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File Tailer integration test
 */
public class FileTailerIT {

  public static final String CONFIG_NAME = "fileTailerITConfig";

  private static final int ENTRY_NUMBER = 1;
  private static final int WRITING_INTERVAL = 1000;
  private static final int SLEEP_TIME = 5000;
  private static final String LOG_MESSAGE = "165.225.156.91 - - [09/Jan/2014:21:28:53 -0400]" +
    " \"GET /index.html HTTP/1.1\" 200 225 \"http://continuuity.com\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"";
  private static final AtomicInteger read = new AtomicInteger();
  private static final  AtomicInteger ingest = new AtomicInteger();

  @Before
  public void prepare() throws Exception {
    deleteTestDir();
  }

  @After
  public void clean() throws Exception {
    deleteTestDir();
  }

  @Test
  public void fileTailerBasicIT() throws Exception {
    read.set(0);
    ingest.set(0);
    File configFile = getConfigFile();
    PipeConfiguration pipeConfig = loadConfig(configFile);

    String logFilePath = pipeConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + pipeConfig.getSourceConfiguration().getFileName();
    Logger logger =  getTimeLogger(logFilePath);
    writeLogs(logger, ENTRY_NUMBER);

    PipeManager manager = new PipeManager(configFile);
    mockMetricsProcessor(manager);
    manager.startAsync();

    writeLogs(logger, ENTRY_NUMBER);
    Thread.sleep(SLEEP_TIME);
    logger.getAppender("File Tailer IT").stop();
    manager.stopAsync();
    Thread.sleep(SLEEP_TIME);
    Assert.assertEquals(read.get(), ingest.get());
  }

  @Test
  public void fileTailerNoLogsBeforeStartIT() throws Exception {
    read.set(0);
    ingest.set(0);
    File configFile = getConfigFile();
    PipeConfiguration pipeConfig = loadConfig(configFile);

    String logFilePath = pipeConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + pipeConfig.getSourceConfiguration().getFileName();
    Logger logger =  getTimeLogger(logFilePath);

    PipeManager manager = new PipeManager(configFile);
    mockMetricsProcessor(manager);
    manager.startAsync();
    Thread.sleep(SLEEP_TIME);

    writeLogs(logger, ENTRY_NUMBER);
    Thread.sleep(SLEEP_TIME);
    logger.getAppender("File Tailer IT").stop();
    manager.stopAsync();
    Thread.sleep(SLEEP_TIME);
    Assert.assertEquals(read.get(), ingest.get());
  }

  private File getConfigFile() throws URISyntaxException{
    String configFileName = System.getProperty(CONFIG_NAME);
    return new File(FileTailerIT.class.getClassLoader().getResource(configFileName).toURI());
  }

  private void deleteTestDir() throws Exception {
    File configFile = getConfigFile();
    PipeConfiguration pipeConfig = loadConfig(configFile);
    File workDir = pipeConfig.getSourceConfiguration().getWorkDir();
    FileUtils.deleteDirectory(workDir);
    File daemonDir = pipeConfig.getDaemonDir();
    FileUtils.deleteDirectory(daemonDir);
  }

  private void writeLogs(Logger logger, int number) throws InterruptedException {
    for (int i = 0; i < number; i++) {
      logger.debug(LOG_MESSAGE);
      Thread.sleep(WRITING_INTERVAL);
    }
  }

  private PipeConfiguration loadConfig(File file) throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(file);
    List<PipeConfiguration> pipeConfig = configuration.getPipeConfigurations();
    return pipeConfig.get(0);
  }

  private Logger getTimeLogger(String file) {

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    RollingFileAppender fileAppender = new RollingFileAppender();
    fileAppender.setName("File Tailer IT");
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
    layoutEncoder.setPattern("%msg%n");
    layoutEncoder.start();
    fileAppender.setEncoder(layoutEncoder);
    fileAppender.start();


    // configures the logger
    Logger logger = loggerContext.getLogger(FileTailerIT.class.getName() + "time");
    logger.setLevel(Level.DEBUG);
    logger.addAppender(fileAppender);
    return logger;
  }

  private void mockMetricsProcessor(PipeManager manager) throws IOException, NoSuchMethodException,
    InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    List<Pipe> pipeList = new ArrayList<Pipe>();
    StreamClient client = null;
    StreamWriter writer = null;
    try {
      Method method1 = manager.getClass().getDeclaredMethod("getPipeConfigs");
      method1.setAccessible(true);
      List<PipeConfiguration> pipeConfList = (List<PipeConfiguration>) method1.invoke(manager);
      for (PipeConfiguration pipeConf : pipeConfList) {
        FileTailerQueue queue = new FileTailerQueue(pipeConf.getQueueSize());
        client = pipeConf.getSinkConfiguration().getStreamClient();
        String streamName = pipeConf.getSinkConfiguration().getStreamName();
        Method method2 = manager.getClass().getDeclaredMethod("getStreamWriterForPipe",
                                                              StreamClient.class, String.class);
        method2.setAccessible(true);
        writer = (StreamWriter) method2.invoke(manager, client, streamName);
        FileTailerStateProcessor stateProcessor =
          new FileTailerStateProcessorImpl(pipeConf.getDaemonDir(), pipeConf.getStateFile());
        FileTailerMetricsProcessor metricsProcessor =
          new FileTailerMetricsProcessor(pipeConf.getDaemonDir(), pipeConf.getStatisticsFile(),
                                         pipeConf.getStatisticsSleepInterval(), pipeConf.getPipeName(),
                                         pipeConf.getSourceConfiguration().getFileName()) {

            @Override
            public void onReadEventMetric(int eventSize) {
              super.onReadEventMetric(eventSize);
              read.incrementAndGet();
            }

            @Override
            public void onIngestEventMetric(int latency) {
              super.onIngestEventMetric(latency);
              ingest.incrementAndGet();
            }
          };
        pipeList.add(new Pipe(new LogTailer(pipeConf, queue, stateProcessor, metricsProcessor),
                              new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                                 stateProcessor, metricsProcessor,
                                                 pipeConf.getSinkConfiguration().getPackSize()),
                              metricsProcessor));
        client = null;
        writer = null;
      }
      Field field = manager.getClass().getDeclaredField("serviceManager");
      field.setAccessible(true);

      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

      field.set(manager, new ServiceManager(pipeList));
    } finally {
      if (client != null) {
        client.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }
}