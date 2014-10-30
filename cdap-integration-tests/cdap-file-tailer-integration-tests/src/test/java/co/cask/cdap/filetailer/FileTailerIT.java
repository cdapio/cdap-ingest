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
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ServiceManager;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File Tailer integration test
 */
public class FileTailerIT {

  public static final String CONFIG_NAME = "fileTailerITConfig";

  private static final int ENTRY_NUMBER = 1;
  private static final int WRITING_INTERVAL = 1000;
  private static final int SLEEP_TIME = 5000;
  private static final String LOG_MESSAGE = "127.0.0.1 - - [01/Jan/2014:21:28:53 -0400]" +
    " \"GET /index.html HTTP/1.1\" 200 225 \"http://example.com\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"";
  private static final AtomicInteger read = new AtomicInteger();
  private static final  AtomicInteger ingest = new AtomicInteger();
  private static final String DEFAULT_AUTH_CLIENT = BasicAuthenticationClient.class.getName();

  private static String cdapHost;
  private static String cdapPort;
  private static Boolean ssl;
  private static String streamName;
  private static Properties tailerProperties;
  private static String authClientPropertiesPath;

  @BeforeClass
  public static void beforeClass() throws URISyntaxException {
    File configFile = getConfigFile();
    tailerProperties = getProperties(configFile);
    cdapHost = tailerProperties.getProperty("pipes.pipe1.sink.host");
    cdapPort = tailerProperties.getProperty("pipes.pipe1.sink.port");
    ssl = Boolean.parseBoolean(tailerProperties.getProperty("pipes.pipe1.sink.ssl"));
    streamName = tailerProperties.getProperty("pipes.pipe1.sink.stream_name");
    authClientPropertiesPath = tailerProperties.getProperty("pipes.pipe1.sink.auth_client_properties");
  }

  @Before
  public void prepare() throws Exception {
    deleteTestDir();
    read.set(0);
    ingest.set(0);
  }

  @After
  public void clean() throws Exception {
    deleteTestDir();
  }

  @Test
  public void fileTailerBasicIT() throws Exception {
    File configFile = getConfigFile();
    PipeConfiguration pipeConfig = loadConfig(configFile);

    String logFilePath = pipeConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + pipeConfig.getSourceConfiguration().getFileName();
    Logger logger =  getTimeLogger(logFilePath);
    writeLogs(logger, ENTRY_NUMBER);

    PipeManager manager = new PipeManager(configFile);
    mockMetricsProcessor(manager);
    long startTime = System.currentTimeMillis();
    manager.startAsync();

    writeLogs(logger, ENTRY_NUMBER);
    Thread.sleep(SLEEP_TIME);
    logger.getAppender("File Tailer IT").stop();
    manager.stopAsync();
    Thread.sleep(SLEEP_TIME);
    Assert.assertEquals(read.get(), ingest.get());
    checkDeliveredEvents(ENTRY_NUMBER * 2, startTime, System.currentTimeMillis());
  }

  @Test
  public void fileTailerNoLogsBeforeStartIT() throws Exception {
    File configFile = getConfigFile();
    PipeConfiguration pipeConfig = loadConfig(configFile);

    String logFilePath = pipeConfig.getSourceConfiguration().getWorkDir().getAbsolutePath() + "/"
      + pipeConfig.getSourceConfiguration().getFileName();
    Logger logger =  getTimeLogger(logFilePath);

    PipeManager manager = new PipeManager(configFile);
    mockMetricsProcessor(manager);
    long startTime = System.currentTimeMillis();
    manager.startAsync();
    Thread.sleep(SLEEP_TIME);

    writeLogs(logger, ENTRY_NUMBER);
    Thread.sleep(SLEEP_TIME);
    logger.getAppender("File Tailer IT").stop();
    manager.stopAsync();
    Thread.sleep(SLEEP_TIME);
    Assert.assertEquals(read.get(), ingest.get());
    checkDeliveredEvents(ENTRY_NUMBER, startTime, System.currentTimeMillis());
  }

  private static File getConfigFile() throws URISyntaxException {
    String configFileName = System.getProperty(CONFIG_NAME);
    Preconditions.checkNotNull(configFileName, CONFIG_NAME + " must be set");
    URL resource = FileTailerIT.class.getClassLoader().getResource(configFileName);
    Preconditions.checkNotNull(resource, "Config file was not found: " + configFileName);
    return new File(resource.toURI());
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
        pipeList.add(new Pipe(new LogTailer(pipeConf, queue, stateProcessor, metricsProcessor, null),
                              new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                                 stateProcessor, metricsProcessor, null,
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

  private static Properties getProperties(File file) {
    Properties properties = new Properties();
    try {
      InputStream is = new FileInputStream(file);
      try {
        properties.load(is);
      } finally {
        Closeables.closeQuietly(is);
      }
    } catch (IOException ignored) {
    }
    return properties;
  }

  private void checkDeliveredEvents(int entryNumber, long startTime, long endTime) throws Exception {
    String eventsStr = readFromStream(startTime, endTime);
    Type listType = new TypeToken<List<StreamEvent>>() { }.getType();
    List<StreamEvent> eventList = new Gson().fromJson(eventsStr, listType);
    Assert.assertEquals(entryNumber, eventList.size());
    for (int i = 0; i < entryNumber; i++) {
      Assert.assertTrue(eventList.get(i).getBody().equals(LOG_MESSAGE));
    }
  }

  private String readFromStream(long startTime, long endTime) throws Exception {
    URI baseUrl = URI.create(String.format("%s://%s:%s", ssl ? "https" : "http",
                                           cdapHost, cdapPort));
    HttpGet getRequest = new HttpGet(baseUrl.resolve(String.format("/v2/streams/%s/events?start=%s&end=%s",
                                                                   streamName, startTime, endTime)));
    if (authClientPropertiesPath != null) {
      AuthenticationClient authClient = configureAuthClient();
      AccessToken token = authClient.getAccessToken();
      getRequest.setHeader("Authorization", token.getTokenType() + " " + token.getValue());
    }
    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(
      new BasicHttpClientConnectionManager()).build();
    HttpResponse response = httpClient.execute(getRequest);
    String res = EntityUtils.toString(response.getEntity());
    Closeables.close(httpClient, true);
    return res;
  }

  private AuthenticationClient configureAuthClient() throws Exception {
    String authClientClassName = getProperty("pipes.pipe1.sink.auth_client", DEFAULT_AUTH_CLIENT);
    AuthenticationClient authClient = (AuthenticationClient) Class.forName(authClientClassName).newInstance();
    InputStream inStream = null;
    try {
      URL resource = FileTailerIT.class.getClassLoader().getResource(authClientPropertiesPath);
      inStream = new FileInputStream(new File(resource.toURI()));
      Properties properties = new Properties();
      properties.load(inStream);
      authClient.configure(properties);
      authClient.setConnectionInfo(cdapHost, Integer.parseInt(cdapPort), ssl);
    } finally {
      try {
        if (inStream != null) {
          inStream.close();
        }
      } catch (IOException ignored) {
      }
    }
    return authClient;
  }

  private static String getProperty(String key, String defaultValue) {
    String value = tailerProperties.getProperty(key);
    return value != null && !value.equals("") ? value : defaultValue;
  }

  private class StreamEvent {
    private Map<String, String> headers;
    private String body;
    long timestamp;

    public String getBody() {
      return body;
    }
  }
}
