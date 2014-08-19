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
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 8/18/14.
 */
public class LogTailerTest {

  private static final Logger LOG = LoggerFactory.getLogger(LogTailerTest.class);

  @Test
  public void basicTest() throws ConfigurationLoaderException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    loader.load(getClass().getClassLoader().getResource("config.properties").getFile());

    FileTailerQueue queue = new FileTailerQueue(10);

//    LogTailer tailer = new LogTailer(loader, );
  }

  @Test
  public void logWriter() {
    org.apache.log4j.Logger logger = initLogger("test");
    WriterAppender appender = initAppender("/home/ytalashko/Logs_test/app.log");

    for (int i = 1; i <= 15; i++) {
      saveEvents(Arrays.asList("log number " + i, "ending " + (100 + i)), logger, appender);

      try {
        Thread.sleep(12000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      saveEvents(Arrays.asList("log number double" + i, "ending " + (1000 + i)), logger, appender);

      try {
        Thread.sleep(17000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private WriterAppender initAppender(String path) {
    LOG.debug("Starting initialize rolling file appender");
    DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
    fileAppender.setFile(path);
    fileAppender.setDatePattern("'.'yyyy-MM-dd-HH-mm");
    fileAppender.setAppend(true);
    fileAppender.setLayout(new PatternLayout("%m%n"));
    fileAppender.activateOptions();
    return fileAppender;
  }

  private org.apache.log4j.Logger initLogger(String name) {
    return org.apache.log4j.Logger.getLogger(name);
  }

  private void saveEvents(List<String> events, org.apache.log4j.Logger logger, WriterAppender fileAppender) {
    LOG.debug("Starting writing {} logs to file", events.size());
    for (String event : events) {
      fileAppender.doAppend(new LoggingEvent(null, logger, null, event, null));
    }
    LOG.debug("{} logs already saved to file", events.size());
  }
}
