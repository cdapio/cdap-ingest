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

package co.cask.cdap.file.dropzone;

import co.cask.cdap.file.dropzone.config.FileDropZoneConfigurationImpl;
import co.cask.cdap.file.dropzone.config.ObserverConfiguration;
import co.cask.cdap.file.dropzone.polling.PollingListener;
import co.cask.cdap.file.dropzone.polling.PollingListenerImpl;
import co.cask.cdap.file.dropzone.polling.PollingService;
import co.cask.cdap.file.dropzone.polling.PollingServiceManager;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File Tailer integration test
 */
public class FileDropZoneIT {

  public static final String CONFIG_NAME = "fileDropZoneITConfig";
  private static final int SLEEP_TIME = 5000;
  private static final String EVENT = "165.225.156.91 - - [09/Jan/2014:21:28:53 -0400]" +
    " \"GET /index.html HTTP/1.1\" 200 225 \"http://continuuity.com\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"";

  private static final AtomicInteger read = new AtomicInteger(0);
  private static final AtomicInteger ingest = new AtomicInteger(0);

  @Before
  public void prepare() throws Exception {
    deleteTestDir();
  }

  @Test
  public void fileDropZoneBasicIT() throws Exception {

    final File configurationFile = getConfigFile();
    PollingServiceManager pollingServiceManager = new PollingServiceManager(configurationFile);
    pollingServiceManager.initManager();

    Field monitor = pollingServiceManager.getClass().getDeclaredField("monitor");
    monitor.setAccessible(true);
    PollingService myMonitor = (PollingService) monitor.get(pollingServiceManager);
    ObserverConfiguration observerConf = loadConfig(configurationFile);
    PollingListener myPollingListener = new PollingListenerImpl(myMonitor, observerConf);
    FileTailerMetricsProcessor metricsProcessor = getMetricsProcessor(observerConf);
    Field metricsProcessorField = myPollingListener.getClass().getDeclaredField("metricsProcessor");
    metricsProcessorField.setAccessible(true);
    metricsProcessorField.set(myPollingListener, metricsProcessor);
    myMonitor.registerDirMonitor(observerConf.getPipeConf().getSourceConfiguration().getWorkDir(), myPollingListener);

    createFile(observerConf.getPipeConf().getSourceConfiguration().getWorkDir().getAbsolutePath());
    pollingServiceManager.startMonitor();
    Thread.sleep(SLEEP_TIME);
    pollingServiceManager.stopMonitor();

    Assert.assertEquals(read.get(), ingest.get());
  }

  private File createFile(String filePath) {
    File file = new File(filePath + "/test");
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(file);
      writer.println(EVENT);
    } catch (IOException ignored) {
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
    return file;
  }


  private FileTailerMetricsProcessor getMetricsProcessor(ObserverConfiguration observerConf) {
    return new FileTailerMetricsProcessor(observerConf.getDaemonDir(),
                                          observerConf.getPipeConf().getStatisticsFile(),
                                          observerConf.getPipeConf().getStatisticsSleepInterval(),
                                          observerConf.getPipeConf().getPipeName(),
                                          observerConf.getPipeConf().
                                            getSourceConfiguration().getWorkDir().getName()) {

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
  }

  private ObserverConfiguration loadConfig(File file) throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(file);
    List<ObserverConfiguration> observerConfig =
      new FileDropZoneConfigurationImpl(configuration.getProperties()).getObserverConfiguration();
    return observerConfig.get(0);
  }


  private File getConfigFile() throws URISyntaxException {
    String configFileName = System.getProperty(CONFIG_NAME);
    return new File(FileDropZoneIT.class.getClassLoader().getResource(configFileName).toURI());
  }

  private void deleteTestDir() throws Exception {
    File configFile = getConfigFile();
    ObserverConfiguration observerConfiguration = loadConfig(configFile);
    File workDir = observerConfiguration.getPipeConf().getSourceConfiguration().getWorkDir();
    FileUtils.deleteDirectory(workDir);
    File daemonDir = observerConfiguration.getPipeConf().getDaemonDir();
    FileUtils.deleteDirectory(daemonDir);
  }

  @After
  public void clean() throws Exception {
    deleteTestDir();
  }
}
