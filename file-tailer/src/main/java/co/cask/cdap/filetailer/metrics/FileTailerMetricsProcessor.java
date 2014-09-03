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

package co.cask.cdap.filetailer.metrics;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import co.cask.cdap.filetailer.BaseWorker;
import co.cask.cdap.filetailer.metrics.exception.FileTailerMetricsProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of FileTailerMetricsProcessor
 */
public class FileTailerMetricsProcessor extends BaseWorker {

  private AtomicInteger totalEventsReadPerFile;
  private AtomicInteger totalEventsIngestedPerFile;

  private AtomicInteger minEventSizePerFile;
  private AtomicInteger maxEventSizePerFile;
  private AtomicInteger totalEventSizePerFile;
  private AtomicInteger eventsPerFile;

  private AtomicInteger minWriteLatencyPerStream;
  private AtomicInteger maxWriteLatencyPerStream;
  private AtomicInteger totalWriteLatencyPerStream;
  private AtomicInteger writesPerStream;

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerMetricsProcessor.class);

  private final String loggerClass = ch.qos.logback.classic.Logger.class.getName();

  private String stateDirPath;

  private String metricsFileName;

  private long metricsSleepInterval;

  private String flowName;
  private String fileName;

  public FileTailerMetricsProcessor(String stateDirPath, String metricsFileName, long metricsSleepInterval,
                                    String flowName, String fileName) {
    this.stateDirPath = stateDirPath;
    this.metricsFileName = metricsFileName;
    this.metricsSleepInterval = metricsSleepInterval;
    this.flowName = flowName;
    this.fileName = fileName;
    initMetrics();
  }

  @Override
  public void run() {
    RollingFileAppender appender = null;
    ch.qos.logback.classic.Logger logger = initLogger("metricsLogger");
    try {
      createDirs(stateDirPath);
      createFile(stateDirPath + "/" + metricsFileName);
      appender = initAppender(stateDirPath, metricsFileName);
      writeMetricsHeader(logger, appender);
      while (!Thread.currentThread().isInterrupted()) {
        Thread.sleep(metricsSleepInterval);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        writeMetrics(logger, appender, currentDate);
        resetMetrics();
      }
    } catch (InterruptedException e) {
      LOG.debug("Metric Processor was interrupted");
    } finally {
      if (appender != null) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        writeMetrics(logger, appender, currentDate);
        appender.stop();
      }
    }
  }

  public void onReadEventMetric(int eventSize) {
    LOG.debug("On Read Event Metric received");
    totalEventsReadPerFile.incrementAndGet();

    totalEventSizePerFile.set(totalEventSizePerFile.get() + eventSize);
    eventsPerFile.incrementAndGet();
    if (minEventSizePerFile.get() > eventSize || minEventSizePerFile.get() == 0) {
      minEventSizePerFile.set(eventSize);
    }
    if (maxEventSizePerFile.get() < eventSize) {
      maxEventSizePerFile.set(eventSize);
    }
  }

  public void onIngestEventMetric(int latency) {
    LOG.debug("On Ingest Event Metric received");
    totalEventsIngestedPerFile.incrementAndGet();

    totalWriteLatencyPerStream.set(totalWriteLatencyPerStream.get() + latency);
    writesPerStream.incrementAndGet();
    if (minWriteLatencyPerStream.get() > latency || minWriteLatencyPerStream.get() == 0) {
      minWriteLatencyPerStream.set(latency);
    }
    if (maxWriteLatencyPerStream.get() < latency) {
      maxWriteLatencyPerStream.set(latency);
    }
  }

  private double calculateAverage(int total, int count) {
    return Math.round(total / (double) count * 1000) / 1000.0;
  }

  private void initMetrics() {
    LOG.debug("Start initializing metrics ..");
    totalEventsReadPerFile = new AtomicInteger(0);
    totalEventsIngestedPerFile = new AtomicInteger(0);

    minEventSizePerFile = new AtomicInteger(0);
    maxEventSizePerFile = new AtomicInteger(0);
    totalEventSizePerFile = new AtomicInteger(0);
    eventsPerFile = new AtomicInteger(0);

    minWriteLatencyPerStream = new AtomicInteger(0);
    maxWriteLatencyPerStream = new AtomicInteger(0);
    totalWriteLatencyPerStream = new AtomicInteger(0);
    writesPerStream = new AtomicInteger(0);
    LOG.debug("All metrics initialized successfully");
  }

  private void resetMetrics() {
    LOG.debug("Starting reset metrics ..");
    totalEventsReadPerFile.set(0);
    totalEventsIngestedPerFile.set(0);

    minEventSizePerFile.set(0);
    maxEventSizePerFile.set(0);
    totalEventSizePerFile.set(0);
    eventsPerFile.set(0);

    minWriteLatencyPerStream.set(0);
    maxWriteLatencyPerStream.set(0);
    totalWriteLatencyPerStream.set(0);
    writesPerStream.set(0);
    LOG.debug("All metrics reset successfully");
  }

  private void writeMetricsHeader(ch.qos.logback.classic.Logger logger, RollingFileAppender appender) {
    LOG.debug("Start writing header to file ..");
    String header = new StringBuilder("Current Date").append(",")
      .append("Flow Name").append(",")
      .append("File Name").append(",")
      .append("Total Events Read Per File").append(",")
      .append("Total Events Ingested Per File").append(",")
      .append("Min Event Size Per File").append(",")
      .append("Average Event Size Per File").append(",")
      .append("Max Event Size Per File").append(",")
      .append("Min Write Latency Per Stream").append(",")
      .append("Average Write Latency Per Stream").append(",")
      .append("Max Write Latency Per Stream").append("\n").toString();
    appender.doAppend(new ch.qos.logback.classic.spi.LoggingEvent(loggerClass, logger, null, header, null, null));
    LOG.debug("Successfully write header");
  }

  private void writeMetrics(ch.qos.logback.classic.Logger logger, RollingFileAppender appender, String currentDate) {
    LOG.debug("Start writing metric with date {} to file ..", currentDate);
    String metric = new StringBuilder(currentDate).append(",")
      .append(flowName).append(",")
      .append(fileName).append(",")
      .append(totalEventsReadPerFile.get()).append(",")
      .append(totalEventsIngestedPerFile.get()).append(",")
      .append(minEventSizePerFile.get()).append(",")
      .append(calculateAverage(totalEventSizePerFile.get(), eventsPerFile.get())).append(",")
      .append(maxEventSizePerFile.get()).append(",")
      .append(minWriteLatencyPerStream.get()).append(",")
      .append(calculateAverage(totalWriteLatencyPerStream.get(), writesPerStream.get())).append(",")
      .append(maxWriteLatencyPerStream.get()).toString();
    appender.doAppend(new ch.qos.logback.classic.spi.LoggingEvent(loggerClass, logger, null, metric, null, null));
    LOG.debug("Successfully write metric with date: {}", currentDate);
  }

  private RollingFileAppender initAppender(String path, String fileName) {
    LOG.debug("Starting initialize rolling file appender");
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    RollingFileAppender fileAppender = new RollingFileAppender();
    fileAppender.setContext(loggerContext);
    fileAppender.setFile(path + "/" + fileName);
    fileAppender.setAppend(true);
    TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setParent(fileAppender);
    rollingPolicy.setFileNamePattern(path + "/" + fileName + ".%d");
    rollingPolicy.start();
    fileAppender.setRollingPolicy(rollingPolicy);
    PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
    layoutEncoder.setContext(loggerContext);
    layoutEncoder.setPattern("%msg%n");
    layoutEncoder.start();
    fileAppender.setEncoder(layoutEncoder);
    fileAppender.start();
    return fileAppender;
  }

  private ch.qos.logback.classic.Logger initLogger(String name) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    return  loggerContext.getLogger(name);
  }

  private void createFile(String path) {
    LOG.debug("Starting create file with path: {}", path);
    File file = new File(path);
    if (!file.exists()) {
      boolean result;
      try {
        result = file.createNewFile();
      } catch (IOException e) {
        throw new FileTailerMetricsProcessorException("Can not create File Tailer metrics file");
      }
      LOG.debug("Creating file result: {}", result);
      if (!result) {
        throw new FileTailerMetricsProcessorException("Can not create File Tailer metrics file");
      }
    } else {
      LOG.debug("Directory/File with path: {} already exist", path);
    }
  }

  private void createDirs(String path) {
    LOG.debug("Starting create directory with path: {}", path);
    File directory = new File(path);
    if (!directory.exists()) {
      boolean result = directory.mkdirs();
      LOG.debug("Creating directory result: {}", result);
      if (!result) {
        throw new FileTailerMetricsProcessorException("Can not create File Tailer state directory");
      }
    } else {
      LOG.debug("Directory/File with path: {} already exist", path);
    }
  }
}
