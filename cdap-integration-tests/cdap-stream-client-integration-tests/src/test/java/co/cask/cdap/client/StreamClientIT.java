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

import co.cask.cdap.client.rest.RestClientConnectionConfig;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.client.rest.RestUtil;
import co.cask.cdap.common.http.exception.HttpFailureException;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StreamClientIT {

  public static final String CONFIG_NAME = "streamClientITConfig";
  public static final String TEST_STREAM = "testStream";

  private StreamClient streamClient;
  private StreamClientTestHelper streamClientTestHelper;
  private AuthenticationClient authClient;
  private String host;
  private int port;
  private boolean ssl;
  private String version;
  private String apiKey;
  private boolean verifySSLCert;
  private int writePoolSize;
  private String authProperties;

  @Before
  public void setUp() throws IOException, KeyManagementException, NoSuchAlgorithmException {
    init();
    streamClient = createTestStreamClient();
    streamClientTestHelper = createTestHelper();
  }

  @Test
  public void testCreateStream() throws IOException {
    String newStreamName = "newStream";
    //Test that we are able to create a stream
    streamClient.create(newStreamName);
    //Get created stream by the name
    JsonObject stream = streamClientTestHelper.getStream(newStreamName);
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
  public void testStreamWriter() throws IOException, ExecutionException, InterruptedException {
    //Create new stream if such stream does not already exist
    streamClient.create(TEST_STREAM);
    //Truncate the stream in the case, if some events already exists
    streamClient.truncate(TEST_STREAM);

    //Test that we are able to write to a stream
    StreamWriter writer = streamClient.createWriter(TEST_STREAM);
    int expectedEventsNum = 10;
    try {
      for (int i = 0; i < expectedEventsNum; i++) {
        writer.write("test" + i, Charset.forName("UTF8"), Collections.singletonMap("key", "value")).get();
      }
    } finally {
      writer.close();
    }
    List<JsonObject> streamEvents = streamClientTestHelper.getStreamEvents(TEST_STREAM);
    Assert.assertEquals(expectedEventsNum, streamEvents.size());

    //Test that we are able to truncate stream
    streamClient.truncate(TEST_STREAM);
    streamEvents = streamClientTestHelper.getStreamEvents(TEST_STREAM);
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
    streamClientTestHelper.close();
    streamClient.close();
  }

  private void init() throws IOException {
    Properties properties = getProperties(System.getProperty(CONFIG_NAME));
    host = properties.getProperty("host");
    port = Integer.valueOf(properties.getProperty("port"));
    ssl = Boolean.valueOf(properties.getProperty("ssl", "false"));
    version = properties.getProperty("version", "v2");
    apiKey = properties.getProperty("apiKey", StringUtils.EMPTY);
    verifySSLCert = Boolean.valueOf(properties.getProperty("verify.ssl.cert", "false"));
    authProperties = properties.getProperty("auth_properties");
    writePoolSize = Integer.valueOf(properties.getProperty("writerPoolSize", "10"));
    authClient = createAuthClient(host, port, ssl);
  }

  private StreamClient createTestStreamClient() throws IOException {
    RestStreamClient.Builder clientBuilder = RestStreamClient.builder(host, port)
      .ssl(ssl)
      .verifySSLCert(verifySSLCert)
      .version(version)
      .writerPoolSize(writePoolSize)
      .authClient(authClient)
      .apiKey(apiKey);
    return clientBuilder.build();
  }

  private AuthenticationClient createAuthClient(String host, int port, boolean ssl) throws IOException {
    AuthenticationClient authClient = new BasicAuthenticationClient();
    if (StringUtils.isNotEmpty(host)) {
      authClient.setConnectionInfo(host, port, ssl);
      if (authProperties != null) {
        Properties authClientProperties = getProperties(authProperties);
        authClient.configure(authClientProperties);
      }
    }
    return authClient;
  }

  private StreamClientTestHelper createTestHelper() throws IOException, NoSuchAlgorithmException,
    KeyManagementException {
    Registry<ConnectionSocketFactory> connectionRegistry = null;
    RestClientConnectionConfig connectionConfig =
      new RestClientConnectionConfig(host, port, authClient, apiKey, ssl, version);
    if (!verifySSLCert) {
      connectionRegistry = RestUtil.getRegistryWithDisabledCertCheck();
    }
    return new StreamClientTestHelper(connectionConfig, connectionRegistry);
  }

  private Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(StreamClientIT.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }
}
