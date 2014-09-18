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
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class StreamClientIT {

  public static final String CONFIG_NAME = "streamClientITConfig";
  public static final String TEST_STREAM = "testStream";

  @Test
  public void testStreamClient() throws IOException {
    StreamClient client = getTestClient();
    //Test that we are able to create a stream
    client.create(TEST_STREAM);

    //Test that we are able to get/set TTL
    long ttl = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
    client.setTTL(TEST_STREAM, ttl);
    Assert.assertEquals(ttl, client.getTTL(TEST_STREAM));

    //Test that we are able to truncate stream
    client.truncate(TEST_STREAM);
  }

  @Test
  public void testStreamWriter() throws IOException {
    StreamClient client = getTestClient();
    //Test that we are able to create a stream
    client.create(TEST_STREAM);
    //Test that we are able to write to a stream
    StreamWriter writer = client.createWriter(TEST_STREAM);
    writer.write("test", Charset.forName("UTF8"), Collections.singletonMap("key", "value"));
    writer.close();
  }

  private StreamClient getTestClient() throws IOException {
    Properties properties = getProperties(System.getProperty(CONFIG_NAME));
    RestStreamClient.Builder clientBuilder = RestStreamClient.builder(properties.getProperty("host"),
                                                                      Integer.valueOf(properties.getProperty("port")));
    clientBuilder.ssl(Boolean.valueOf(properties.getProperty("ssl", "false")));
    clientBuilder.disableCertCheck(Boolean.valueOf(properties.getProperty("disableCertCheck", "false")));
    clientBuilder.version(properties.getProperty("version", "v2"));
    clientBuilder.writerPoolSize(Integer.valueOf(properties.getProperty("writerPoolSize", "10")));

    if (properties.getProperty("host") != null) {
      AuthenticationClient client = new BasicAuthenticationClient();
      client.setConnectionInfo(properties.getProperty("host"),
                               Integer.valueOf(properties.getProperty("port")),
                               Boolean.valueOf(properties.getProperty("ssl", "false")));
      String authProperties = properties.getProperty("auth_properties");
      if (authProperties != null) {
        Properties authClientProperties = getProperties(authProperties);
        client.configure(authClientProperties);
        clientBuilder.authClient(client);
      }
    }

    return clientBuilder.build();
  }

  private Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(StreamClientIT.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }
}
