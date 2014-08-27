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

import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Properties;

public class ConfigurationLoaderImplTest {

  @Test
  public void loadTest() throws ConfigurationLoadingException, NoSuchFieldException,
                                              IllegalAccessException, URISyntaxException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String path = getClass().getClassLoader().getResource("test.properties").getFile();

    Configuration configuration = loader.load(path);

    Field field = configuration.getClass().getDeclaredField("properties");
    field.setAccessible(true);
    Properties properties = (Properties) field.get(configuration);

    Assert.assertEquals(25, properties.size());
  }

  @Test(expected = ConfigurationLoadingException.class)
  public void loadFailureTest() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String fakePath = "fake path";

    loader.load(fakePath);
  }
}
