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
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;


public class StreamClientIT {

  public static final String CONFIG_NAME = "stream_client_it_config";

  @Test
  public void testStreamCreate() throws IOException {
    StreamClient client = getTestClient();
    client.create("testStream");

    long ttl = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    client.setTTL("testStream", ttl);
    Assert.assertEquals(ttl, client.getTTL("testStream"));
  }

  private StreamClient getTestClient() throws IOException {
    Properties properties = getProperties();
    return RestStreamClient.builder(properties.getProperty("host"),
                                    Integer.valueOf(properties.getProperty("port"))).build();
  }

  private Properties getProperties() throws IOException {
    Properties properties = new Properties();
    properties.load(StreamClientIT.class.getClassLoader().getResourceAsStream(System.getProperty(CONFIG_NAME)));
    return properties;
  }
}
