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
import co.cask.cdap.utils.StreamReader;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.agent.embedded.EmbeddedAgent;
import org.apache.flume.agent.embedded.EmbeddedAgentConfiguration;
import org.apache.flume.event.SimpleEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

  private static StreamReader streamReader;
  private static String streamName;

  @Before
  public void setUP() throws Exception {
    Field field = EmbeddedAgentConfiguration.class.getDeclaredField("ALLOWED_SINKS");
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    String sinkList[] = {"co.cask.cdap.flume.StreamSink"};
    field.set(null, sinkList);
    Properties flumeProperties = StreamReader.getProperties(System.getProperty(CONFIG_NAME));
    streamReader = StreamReader.builder()
      .setProperties(flumeProperties)
      .setCdapHost(flumeProperties.getProperty("sink1.host"))
      .setCdapPort(Integer.valueOf(flumeProperties.getProperty("sink1.port")))
      .setSSL(Boolean.parseBoolean(flumeProperties.getProperty("sink1.ssl")))
      .setAuthClientPropertiesPath(flumeProperties.getProperty("sink1.authClientProperties"))
      .build();

    streamName = flumeProperties.getProperty("sink1.streamName");

    if (streamReader.getAuthClientPropertiesPath() != null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(streamReader.getAuthClientPropertiesPath());
      flumeProperties.setProperty(AUTH_PROPERTIES, url != null ? url.getPath() : StringUtils.EMPTY);
    }
    createStream();
  }

  @Test
  public void baseEventProcessingTest() throws Exception {
    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Map<String, String> propertyMap = new HashMap<String, String>();
    for (final String name : streamReader.getProperties().stringPropertyNames()) {
      propertyMap.put(name, streamReader.getProperties().getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();

    checkDeliveredEvents(startTime);
  }

  /**
   * Test simulate CDAP fail. CDAP port is swapped in runtime to simulate work with not accessible port.
   */
  @Test
  public void eventProcessingWithCdapFailTest() throws Exception {
    String cdapPortStrValue = String.valueOf(streamReader.getCdapPort());
    EmbeddedAgent agent = new EmbeddedAgent("test-flume");
    Method method = agent.getClass().getDeclaredMethod("doConfigure", Map.class);
    method.setAccessible(true);
    Map<String, String> propertyMap = new HashMap<String, String>();
    for (final String name : streamReader.getProperties().stringPropertyNames()) {
      propertyMap.put(name, streamReader.getProperties().getProperty(name));
    }
    agent.configure(propertyMap);
    agent.start();
    method.invoke(agent, propertyMap);
    long startTime = System.currentTimeMillis();
    writeEvents(agent, 0, EVENT_FAIL_START_NUMBER);
    Assert.assertNotEquals(cdapPortStrValue, INVALID_PORT);
    propertyMap.put("sink1.port", INVALID_PORT);
    method.invoke(agent, propertyMap);
    writeEvents(agent, EVENT_FAIL_START_NUMBER, FAIL_EVENT_END_NUMBER);
    propertyMap.put("sink1.port", cdapPortStrValue);
    method.invoke(agent, propertyMap);

    writeEvents(agent, FAIL_EVENT_END_NUMBER, EVENT_NUMBER);
    Thread.sleep(SLEEP_INTERVAL);
    agent.stop();

    checkDeliveredEvents(startTime);
  }

  private void checkDeliveredEvents(long startTime) throws Exception {
    List<String> events = streamReader.getDeliveredEvents(streamName, startTime, System.currentTimeMillis());
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
    RestStreamClient.Builder builder = RestStreamClient.builder(streamReader.getCdapHost(), streamReader.getCdapPort())
      .ssl(streamReader.getSsl());
    if (streamReader.getAuthClientPropertiesPath() != null) {
      AuthenticationClient authClient = streamReader.createAuthClient();
      builder.authClient(authClient);
    }
    StreamClient streamClient = builder.build();
    streamClient.create(streamName);
  }

  @After
  public void shutDown() throws Exception {
    streamReader.close();
  }
}
