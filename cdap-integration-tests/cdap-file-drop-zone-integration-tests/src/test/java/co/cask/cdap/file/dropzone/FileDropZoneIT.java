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
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import co.cask.cdap.utils.StreamReader;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File DropZone integration test
 */
public class FileDropZoneIT {

  public static final String CONFIG_NAME = "fileDropZoneITConfig";
  private static final int SLEEP_TIME = 5000;
  private static final String EVENT = "165.225.156.91 - - [09/Jan/2014:21:28:53 -0400]" +
    " \"GET /index.html HTTP/1.1\" 200 225 \"http://continuuity.com\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"";
  private static final String EMPTY_EVENT = "";
  private static final int EVENT_NUMBER = 3;
  private static final String DEFAULT_AUTH_CLIENT = BasicAuthenticationClient.class.getName();
  private static final AtomicInteger read = new AtomicInteger(0);
  private static final AtomicInteger ingest = new AtomicInteger(0);

  private static StreamReader streamReader;
  private static String streamName;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Properties dropZoneProperties = StreamReader.getProperties(System.getProperty(CONFIG_NAME));
    streamReader = StreamReader.builder().setCdapHost(dropZoneProperties.getProperty("pipes.pipe1.sink.host"))
      .setCdapPort(Integer.valueOf(dropZoneProperties.getProperty("pipes.pipe1.sink.port")))
      .setSSL(Boolean.parseBoolean(dropZoneProperties.getProperty("pipes.pipe1.sink.ssl")))
      .setVerifySSLCert(Boolean.valueOf(dropZoneProperties.getProperty("pipes.pipe1.sink.verify.ssl.cert")))
      .setAuthClientPropertiesPath(dropZoneProperties.getProperty("pipes.pipe1.sink.auth_client_properties"))
      .setAuthClientClassName(dropZoneProperties.getProperty("pipes.pipe1.sink.auth_client", DEFAULT_AUTH_CLIENT))
      .build();
    streamName = dropZoneProperties.getProperty("pipes.pipe1.sink.stream_name");
  }

  @Before
  public void prepare() throws Exception {
    deleteTestDir();
    read.set(0);
    ingest.set(0);
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
    long start = System.currentTimeMillis();
    pollingServiceManager.startMonitor();
    Thread.sleep(SLEEP_TIME);
    pollingServiceManager.stopMonitor();
    checkDeliveredEvents(start, System.currentTimeMillis());
    Assert.assertEquals(read.get(), ingest.get());
  }

  private void checkDeliveredEvents(long startTime, long endTime) throws Exception {
    List<String> eventList = streamReader.getDeliveredEvents(streamName, startTime, endTime);
    Assert.assertEquals(EVENT_NUMBER, eventList.size());
    Assert.assertTrue(eventList.get(0).equals(EVENT));
    Assert.assertTrue(eventList.get(1).equals(EMPTY_EVENT));
    Assert.assertTrue(eventList.get(2).equals(EVENT));
  }

  private File createFile(String filePath) {
    File file = new File(filePath + "/test");
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(file);
      writer.println(EVENT);
      writer.println(EMPTY_EVENT);
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

  @AfterClass
  public static void shutDown() throws Exception {
    streamReader.close();
  }
}
