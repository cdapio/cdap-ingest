
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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 */
public class  TailerLogUtils {

  public static void writeLineToFile(String filePath, String line) throws IOException {
    Writer writer = new FileWriter(filePath, true);
    writer.write(line + "\n");
    writer.flush();
    writer.close();
  }

  public static PipeConfiguration loadConfig() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Class<? extends Class> path1 = TailerLogUtils.class.getClass();
    String path = TailerLogUtils.class.getClassLoader().getResource("test4.properties").getFile();
    Configuration configuration = loader.load(new File(path));
    List<PipeConfiguration> flowConfig = configuration.getPipesConfiguration();
    return flowConfig.get(0);
  }

  public static void createTestDirIfNeed() throws ConfigurationLoadingException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    File dirFile = new File(dir);
    if (!dirFile.exists()) {
      dirFile.mkdir();
    }
  }

  public static void clearTestDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.cleanDirectory(new File(dir));
  }

  public static void clearStateDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getDaemonDir();
    FileUtils.cleanDirectory(new File(dir));
  }

  public static void deleteTestDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.deleteDirectory(new File(dir));
  }

  public static ch.qos.logback.classic.Logger getSizeLogger(String file, String fileSize) {

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    ch.qos.logback.core.rolling.RollingFileAppender fileAppender =
      new ch.qos.logback.core.rolling.RollingFileAppender();
    fileAppender.setContext(loggerContext);
    fileAppender.setFile(file);
    fileAppender.setAppend(true);
    FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setFileNamePattern(file + "%i");
    rollingPolicy.setParent(fileAppender);
    rollingPolicy.start();
    rollingPolicy.setMaxIndex(100);
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
    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(BaseTailerTest.class.getName() + "size");
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    rootLogger.addAppender(fileAppender);
    return rootLogger;

  }

  public static ch.qos.logback.classic.Logger getTimeLogger(String file) {

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
    ch.qos.logback.classic.Logger rootLogger =
                    loggerContext.getLogger(BaseTailerTest.class.getName() + "time");
    rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    rootLogger.addAppender(fileAppender);
    return rootLogger;
  }

  public static LogTailer  createTailer(FileTailerQueue queue, PipeConfiguration flowConfig)
                           throws ConfigurationLoadingException {
    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(flowConfig.getDaemonDir(), flowConfig.getStateFile());
    FileTailerMetricsProcessor metricsProcessor =
      new FileTailerMetricsProcessor(flowConfig.getDaemonDir(), flowConfig.getStatisticsFile(),
                                     flowConfig.getStatisticsSleepInterval(), flowConfig.getPipeName(),
                                     flowConfig.getSourceConfiguration().getFileName());
    LogTailer tailer = new LogTailer(TailerLogUtils.loadConfig(), queue, stateProcessor, metricsProcessor);
    return tailer;
  }
}
