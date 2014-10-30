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
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.utils.EventUtil;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.agent.embedded.EmbeddedAgent;
import org.apache.flume.agent.embedded.EmbeddedAgentConfiguration;
import org.apache.flume.event.SimpleEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  private static EventUtil eventUtil;

  @Before
  public void setUP() throws Exception {
    Field field = EmbeddedAgentConfiguration.class.getDeclaredField("ALLOWED_SINKS");
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    String sinkList[] = {"co.cask.cdap.flume.StreamSink"};
    field.set(null, sinkList);
    Properties flumeProperties = getProperties(System.getProperty(CONFIG_NAME));
    EventUtil.Builder builder = EventUtil.builder();

    builder.setProperties(flumeProperties);
    builder.setCdapHost(flumeProperties.getProperty("sink1.host"));
    builder.setCdapPort(flumeProperties.getProperty("sink1.port"));
    builder.setSSL(Boolean.parseBoolean(flumeProperties.getProperty("sink1.ssl")));
    builder.setStreamName(flumeProperties.getProperty("sink1.streamName"));
    builder.setAuthClientPropertiesPath(flumeProperties.getProperty("sink1.authClientProperties"));
    eventUtil = builder.build();

    if (eventUtil.getAuthClientPropertiesPath() != null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(eventUtil.getAuthClientPropertiesPath());
      flumeProperties.setProperty(AUTH_PROPERTIES, url.getPath());
    }
    createStream();
  }

  @Test
  public void baseEventProcessingTest() throws Exception {
    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Map propertyMap = new HashMap<String, String>();
    for (final String name : eventUtil.getProperties().stringPropertyNames()) {
      propertyMap.put(name, eventUtil.getProperties().getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();

    List<String> events = eventUtil.getDeliveredEvents(startTime, System.currentTimeMillis());
    Assert.assertEquals(EVENT_NUMBER, events.size());
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Assert.assertTrue(events.get(i).equals(EVENT_STR + i));
    }
  }

  /**
   * Test simulate CDAP fail. CDAP port is swapped in runtime to simulate work with not accessible port.
   */
  @Test
  public void eventProcessingWithCdapFailTest() throws Exception {

    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Method method = agent.getClass().getDeclaredMethod("doConfigure", Map.class);
    method.setAccessible(true);
    Map propertyMap = new HashMap<String, String>();
    for (final String name : eventUtil.getProperties().stringPropertyNames()) {
      propertyMap.put(name, eventUtil.getProperties().getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    method.invoke(agent, propertyMap);
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_FAIL_START_NUMBER);
    Assert.assertNotEquals(eventUtil.getCdapPort(), INVALID_PORT);
    propertyMap.put("sink1.port", INVALID_PORT);
    method.invoke(agent, propertyMap);
    writeEvents(agent, EVENT_FAIL_START_NUMBER, FAIL_EVENT_END_NUMBER);
    propertyMap.put("sink1.port", eventUtil.getCdapPort());
    method.invoke(agent, propertyMap);

    writeEvents(agent, FAIL_EVENT_END_NUMBER, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();

    List<String> events = eventUtil.getDeliveredEvents(startTime, System.currentTimeMillis());
    Assert.assertEquals(EVENT_NUMBER, events.size());
    for (int i = 0; i < EVENT_NUMBER; i++) {
      Assert.assertTrue(events.get(i).equals(EVENT_STR + i));
    }
  }

  private void writeEvents(EmbeddedAgent agent, int startNumber, int endNumber) {
    for (int i = startNumber; i < endNumber; i++) {
      Event event = new SimpleEvent();
      event.setBody((EVENT_STR + i).getBytes());
      try {
        agent.put(event);
      } catch (EventDeliveryException ignored) {
      }
    }
  }

  private void createStream() throws Exception {
    RestStreamClient.Builder builder = RestStreamClient.builder(eventUtil.getCdapHost(),
                                                                Integer.parseInt(eventUtil.getCdapPort()));
    builder.ssl(eventUtil.getSsl());
    if (eventUtil.getAuthClientPropertiesPath() != null) {
      AuthenticationClient authClient = eventUtil.configureAuthClient();
      builder.authClient(authClient);
    }
    StreamClient streamClient = builder.build();
    streamClient.create(eventUtil.getStreamName());
  }

  private Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(CdapFlumeIT.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }
}
