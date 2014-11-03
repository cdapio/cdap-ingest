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

package co.cask.cdap.client;

import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.common.http.exception.HttpFailureException;
import co.cask.cdap.utils.StreamReader;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class StreamClientIT {

  private static final String CONFIG_NAME = "streamClientITConfig";
  private static final String TEST_STREAM = "testStream";
  private static final String EVENT_STR_PREFIX = "Test event number: ";

  private StreamClient streamClient;
  private StreamReader streamReader;
  private String host;
  private int port;
  private boolean ssl;
  private String version;
  private String apiKey;
  private boolean verifySSLCert;
  private int writePoolSize;
  private String authProperties;

  @Before
  public void setUp() throws Exception {
    init();
    streamClient = createTestStreamClient();
  }

  @Test
  public void testCreateStream() throws IOException {
    String newStreamName = "newStream";
    //Test that we are able to create a stream
    streamClient.create(newStreamName);
    //Get created stream by the name
    JsonObject stream = streamReader.getStream(newStreamName);
    Assert.assertNotNull(stream);
    Assert.assertEquals(newStreamName, stream.get("name").getAsString());
  }

  @Test
  public void testSetAndGetTTL() throws IOException {
    streamClient.create(TEST_STREAM);
    //Test that we are able to get/set TTL
    long ttl = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
    streamClient.setTTL(TEST_STREAM, ttl);
    Assert.assertEquals(ttl, streamClient.getTTL(TEST_STREAM));
  }

  @Test
  public void testStreamWriter() throws Exception {
    //Create new stream if such stream does not already exist
    streamClient.create(TEST_STREAM);
    //Truncate the stream in the case, if some events already exists
    streamClient.truncate(TEST_STREAM);

    //Test that we are able to write to a stream
    StreamWriter writer = streamClient.createWriter(TEST_STREAM);
    int expectedEventsNum = 10;
    try {
      for (int i = 0; i < expectedEventsNum; i++) {
        writer.write(EVENT_STR_PREFIX + i, Charset.forName("UTF8"), Collections.singletonMap("key", "value")).get();
      }
    } finally {
      writer.close();
    }
    //Check events from the stream
    List<String> streamEvents = streamReader.getDeliveredEvents(TEST_STREAM);
    Assert.assertEquals(expectedEventsNum, streamEvents.size());
    for (int i = 0; i < expectedEventsNum; i++) {
      Assert.assertEquals(EVENT_STR_PREFIX + i, streamEvents.get(i));
    }

    //Test that we are able to truncate stream
    streamClient.truncate(TEST_STREAM);
    streamEvents = streamReader.getDeliveredEvents(TEST_STREAM);
    Assert.assertEquals(0, streamEvents.size());
  }

  @Test
  public void testCreateWriterWithNonExistingStream() throws IOException {
    //Test that we aren't able to create writer for non existing event
    try {
      streamClient.createWriter("test");
      Assert.fail("HttpFailureException expected");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpStatus.SC_NOT_FOUND, e.getStatusCode());
    }
  }

  @After
  public void shutDown() throws IOException {
    streamReader.close();
    streamClient.close();
  }

  private void init() throws Exception {
    Properties properties = StreamReader.getProperties(System.getProperty(CONFIG_NAME));
    host = properties.getProperty("host");
    port = Integer.valueOf(properties.getProperty("port"));
    ssl = Boolean.valueOf(properties.getProperty("ssl", "false"));
    version = properties.getProperty("version", "v2");
    apiKey = properties.getProperty("apiKey", StringUtils.EMPTY);
    verifySSLCert = Boolean.valueOf(properties.getProperty("verify.ssl.cert", "false"));
    authProperties = properties.getProperty("auth_properties");
    writePoolSize = Integer.valueOf(properties.getProperty("writerPoolSize", "10"));
    streamReader = createStreamReader();
  }

  private StreamClient createTestStreamClient() throws Exception {
    RestStreamClient.Builder clientBuilder = RestStreamClient.builder(host, port)
      .ssl(ssl)
      .verifySSLCert(verifySSLCert)
      .version(version)
      .writerPoolSize(writePoolSize)
      .authClient(streamReader.createAuthClient())
      .apiKey(apiKey);
    return clientBuilder.build();
  }

  private StreamReader createStreamReader() throws Exception {
    return StreamReader.builder()
      .setCdapHost(host)
      .setCdapPort(port)
      .setAuthClientPropertiesPath(authProperties)
      .setSSL(ssl)
      .setVerifySSLCert(verifySSLCert)
      .build();
  }
}
