/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.filetailer.config;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.config.exception.ConfigurationResolvException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

public class ConfigurationLoaderImplTest {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoaderImplTest.class);

  @Test
  public void initTest() throws ConfigurationLoaderException, NoSuchFieldException,
                                              IllegalAccessException, URISyntaxException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String path = getClass().getClassLoader().getResource("test.properties").getFile();

    loader.load(path);

    Field field = loader.getClass().getDeclaredField("properties");
    field.setAccessible(true);
    Properties properties = (Properties) field.get(loader);

    Assert.assertEquals(24, properties.size());
  }

  @Test(expected = ConfigurationLoaderException.class)
  public void initFailureTest() throws ConfigurationLoaderException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String fakePath = "fake path";

    loader.load(fakePath);
  }

  @Test
  public void loadPropertiesTest() throws ConfigurationLoaderException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String path = getClass().getClassLoader().getResource("test.properties").getFile();

    loader.load(path);

    List<StreamClient> streamClients = loader.getStreamClients();

    Assert.assertEquals(2, streamClients.size());

    String streamName = loader.getStreamName();

    Assert.assertEquals("name", streamName);

    String charsetName = loader.getCharsetName();

    Assert.assertEquals("UTF-8", charsetName);

    String sinkStrategy = loader.getSinkStrategy();

    Assert.assertEquals("failover", sinkStrategy);

    String workDir = loader.getWorkDir();

    Assert.assertEquals("/home/user/log/", workDir);

    String fileName = loader.getFileName();

    Assert.assertEquals("app.log", fileName);

    String rotationPattern = loader.getRotationPattern();

    Assert.assertEquals("yyyy-MM-dd-HH-mm", rotationPattern);

    String stateDir = loader.getStateDir();

    Assert.assertEquals("/home/user/file_tailer_tmp", stateDir);

    String stateFile = loader.getStateFile();

    Assert.assertEquals("file_tailer.state", stateFile);

    int failureRetryLimit = loader.getFailureRetryLimit();

    Assert.assertEquals(5, failureRetryLimit);

    byte recordSeparator = loader.getRecordSeparator();

    Assert.assertEquals("\n".getBytes()[0], recordSeparator);

    long sleepInterval = loader.getSleepInterval();

    Assert.assertEquals(3000, sleepInterval);

    int queueSize = loader.getQueueSize();

    Assert.assertEquals(100, queueSize);
  }

  @Test(expected = ConfigurationResolvException.class)
  public void loadPropertiesFailureTest() {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    loader.getFileName();
  }
}
