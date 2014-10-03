
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
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class CdapFlumeIT {

  private static final int eventNumber = 50;
  private static final String CONFIG_NAME = "cdapFlumeITConfig";
  private static final String AUTH_PROPERTIES = "sink1.authClientProperties";
  private static final String EVENT_STR = "Test event number: ";
  private static final String RESPONSE_EVENT_BODY = "body";

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
    for (int i = 0; i < eventNumber; i++) {
      Event event = new SimpleEvent();
      event.setBody((EVENT_STR + i).getBytes());
      agent.put(event);
    }
    Thread.currentThread().sleep(1000);
    agent.stop();
    HttpResponse response = readFromStream(properties, startTime, System.currentTimeMillis());
    JSONArray jsonArray = new JSONArray(EntityUtils.toString(response.getEntity()));
    for (int i = 0; i < eventNumber; i++) {
      Assert.assertTrue(jsonArray.getJSONObject(i).getString(RESPONSE_EVENT_BODY).equals(EVENT_STR + i));
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
    long startTime = System.currentTimeMillis();
    agent.configure(propertyMap);
    agent.start();
    for (int i = 0; i < eventNumber; i++) {
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

  private HttpResponse readFromStream(Properties properties, long startTime, long endTime) throws Exception {
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

      Properties authProperties = new Properties();
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
    return response;
  }

  private Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(CdapFlumeIT.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }
}
