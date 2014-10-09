
/*
 * Copyright © 2014 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package co.cask.cdap.flume;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import com.google.common.io.Closeables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.agent.embedded.EmbeddedAgent;
import org.apache.flume.agent.embedded.EmbeddedAgentConfiguration;
import org.apache.flume.event.SimpleEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class CdapFlumeIT {

  private static final int EVENT_NUMBER = 50;
  private static final String CONFIG_NAME = "cdapFlumeITConfig";
  private static final String AUTH_PROPERTIES = "sink1.authClientProperties";
  private static final String EVENT_STR = "Test event number: ";
  private static final long SLEEP_INTERVAL = 1000;
  public static final String INVALID_PORT = "11111";
  public static final int EVENT_FAIL_START_NUMBER = 10;
  public static final int FAIL_EVENT_END_NUMBER = 20;

  private String cdapPort;
  private String cdapHost;
  private Boolean ssl;
  private String streamId;
  private Properties flumeProperties;
  private String authClientPropertiesPath;

  @Test
  public void baseEventProcessingTest() throws Exception {
    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Map propertyMap = new HashMap<String, String>();
    for (final String name : flumeProperties.stringPropertyNames()) {
      propertyMap.put(name, flumeProperties.getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();
    checkDeliveredEvents(flumeProperties, startTime, System.currentTimeMillis());
  }

  @Test
  /**
   * Test simulate CDAP fail. CDAP port is swapped in runtime to simulate work with not accessible port.
   */
  public void eventProcessingWithCdapFailTest() throws Exception {

    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Method method = agent.getClass().getDeclaredMethod("doConfigure", Map.class);
    method.setAccessible(true);
    Map propertyMap = new HashMap<String, String>();
    for (final String name : flumeProperties.stringPropertyNames()) {
      propertyMap.put(name, flumeProperties.getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    method.invoke(agent, propertyMap);
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_FAIL_START_NUMBER);
    Assert.assertNotEquals(cdapPort, INVALID_PORT);
    propertyMap.put("sink1.port", INVALID_PORT);
    method.invoke(agent, propertyMap);
    writeEvents(agent, EVENT_FAIL_START_NUMBER, FAIL_EVENT_END_NUMBER);
    propertyMap.put("sink1.port", cdapPort);
    method.invoke(agent, propertyMap);

    writeEvents(agent, FAIL_EVENT_END_NUMBER, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();

    checkDeliveredEvents(flumeProperties, startTime, System.currentTimeMillis());
  }

  private void checkDeliveredEvents(Properties properties, long startTime, long endTime) throws Exception {
    String eventsStr = readFromStream(properties, startTime, endTime);
    Type listType = new TypeToken<List<StreamEvent>>() { }.getType();
    List<StreamEvent> eventList = new Gson().fromJson(eventsStr, listType);
    Assert.assertEquals(EVENT_NUMBER, eventList.size());
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Assert.assertTrue(eventList.get(i).getBody().equals(EVENT_STR + i));
    }
  }

  @Before
  public void setUP() throws Exception {
    Field field = EmbeddedAgentConfiguration.class.getDeclaredField("ALLOWED_SINKS");
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    String sinkList[] = {"co.cask.cdap.flume.StreamSink"};
    field.set(null, sinkList);
    flumeProperties = getProperties(System.getProperty(CONFIG_NAME));

    cdapHost = flumeProperties.getProperty("sink1.host");
    cdapPort = flumeProperties.getProperty("sink1.port");
    ssl = Boolean.parseBoolean(flumeProperties.getProperty("sink1.ssl"));
    streamId = flumeProperties.getProperty("sink1.streamName");
    authClientPropertiesPath = flumeProperties.getProperty("sink1.authClientProperties");
    if (authClientPropertiesPath != null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(authClientPropertiesPath);
      flumeProperties.setProperty(AUTH_PROPERTIES, url.getPath());
      authClientPropertiesPath = url.getPath();
    }
    createStream();

  }

  private void writeEvents(EmbeddedAgent agent, int startNumber, int endNumber) {
    for (int i = startNumber; i < endNumber; i++) {
      Event event = new SimpleEvent();
      event.setBody((EVENT_STR + i).getBytes());
      try {
        agent.put(event);
      } catch (EventDeliveryException e) {
      }
    }
  }

  private void createStream() throws Exception {
    RestStreamClient.Builder builder = RestStreamClient.builder(cdapHost, Integer.parseInt(cdapPort));
    builder.ssl(ssl);
    if (authClientPropertiesPath != null) {
      AuthenticationClient authClient = configureAuthClient();
      builder.authClient(authClient);
    }
    StreamClient streamClient = builder.build();
    streamClient.create(streamId);
  }


  private String readFromStream(Properties properties, long startTime, long endTime) throws Exception {
    URI baseUrl = URI.create(String.format("%s://%s:%s", ssl ? "https" : "http",
                                           cdapHost, cdapPort));
    HttpGet getRequest = new HttpGet(baseUrl.resolve(String.format("/v2/streams/%s/events?start=%s&end=%s",
                                                                   streamId, startTime, endTime)));
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
    String authClientClassName = flumeProperties.getProperty("sink1.authClientClass");
    AuthenticationClient authClient = (AuthenticationClient) Class.forName(authClientClassName).newInstance();
    InputStream inStream = null;
    try {
      inStream = new FileInputStream(authClientPropertiesPath);
      Properties properties = new Properties();
      properties.load(inStream);
      authClient.configure(properties);
      authClient.setConnectionInfo(cdapHost, Integer.parseInt(cdapPort), ssl);
    } finally {
      try {
        if (inStream != null) {
          inStream.close();
        }
      } catch (IOException e) {
      }
    }
    return authClient;
  }

  private Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(CdapFlumeIT.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }

  class StreamEvent {
    private Map<String, String> headers;
    private String body;
    long timestamp;

    public String getBody() {
      return body;
    }
  }

}
