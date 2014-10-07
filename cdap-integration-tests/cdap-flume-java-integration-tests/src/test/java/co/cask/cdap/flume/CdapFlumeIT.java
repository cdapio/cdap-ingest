
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

  @Test
  public void baseEventProcessingTest() throws Exception {
    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Properties properties = getProperties(System.getProperty(CONFIG_NAME));
    String authPath = properties.getProperty(AUTH_PROPERTIES);
    if (authPath != null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(authPath);
      properties.setProperty(AUTH_PROPERTIES, url.getPath());
    }
    Map propertyMap = new HashMap<String, String>();
    for (final String name : properties.stringPropertyNames()) {
      propertyMap.put(name, properties.getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Event event = new SimpleEvent();
      event.setBody((EVENT_STR + i).getBytes());
      agent.put(event);
    }
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();
    String eventsStr = readFromStream(properties, startTime, System.currentTimeMillis());
    Type listType = new TypeToken<List<StreamEvent>>() {
    }.getType();
    List<StreamEvent> eventList = new Gson().fromJson(eventsStr, listType);
    Assert.assertEquals(EVENT_NUMBER, eventList.size());
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Assert.assertTrue(eventList.get(i).getBody().equals(EVENT_STR + i));
    }
  }

  @Test(expected = EventDeliveryException.class)
  public void failEventProcessingTest() throws Exception {

    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Properties properties = getProperties(System.getProperty(CONFIG_NAME));
    properties.setProperty("sink1.port", "11111");
    String authPath = properties.getProperty(AUTH_PROPERTIES);
    if (authPath != null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(authPath);
      properties.setProperty(AUTH_PROPERTIES, url.getPath());
    }
    Map propertyMap = new HashMap<String, String>();
    for (final String name : properties.stringPropertyNames()) {
      propertyMap.put(name, properties.getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Event event = new SimpleEvent();
      agent.put(event);
    }
    agent.stop();
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
  }

  private String readFromStream(Properties properties, long startTime, long endTime) throws Exception {
    String host = properties.getProperty("sink1.host");
    String port = properties.getProperty("sink1.port");
    boolean ssl = Boolean.parseBoolean(properties.getProperty("sink1.ssl"));
    String streamId = properties.getProperty("sink1.streamName");
    String authClientPropertiesPath = properties.getProperty("sink1.authClientProperties");
    URI baseUrl = URI.create(String.format("%s://%s:%s", ssl ? "https" : "http",
                                           host, port));
    HttpGet getRequest = new HttpGet(baseUrl.resolve(String.format("/v2/streams/%s/events?start=%s&end=%s",
                                                                   streamId, startTime, endTime)));
    if (authClientPropertiesPath != null) {
      String authClientClassName = properties.getProperty(
        "sink1.authClientClass", "co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient");
      AuthenticationClient authClient = (AuthenticationClient) Class.forName(authClientClassName).newInstance();

      InputStream inStream = null;
      try {
        inStream = new FileInputStream(authClientPropertiesPath);
        properties.load(inStream);
        authClient.configure(properties);
        authClient.setConnectionInfo(host, Integer.parseInt(port), ssl);
        AccessToken token = authClient.getAccessToken();
        getRequest.setHeader("Authorization", token.getTokenType() + " " + token.getValue());
      } finally {
        try {
          if (inStream != null) {
            inStream.close();
          }
        } catch (IOException e) {
        }
      }
    }

    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(
      new BasicHttpClientConnectionManager()).build();
    HttpResponse response = httpClient.execute(getRequest);
    String res = EntityUtils.toString(response.getEntity());
    Closeables.close(httpClient, true);
    return res;
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
