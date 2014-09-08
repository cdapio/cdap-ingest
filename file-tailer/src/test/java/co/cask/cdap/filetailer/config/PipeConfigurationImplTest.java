/*
 * Copyright 2014 Cask Data, Inc.
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

import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class PipeConfigurationImplTest {

  @Test
  public void basicTest() throws ConfigurationLoadingException {

    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String path = getClass().getClassLoader().getResource("test.properties").getFile();

    Configuration configuration = loader.load(new File(path));

    List<PipeConfiguration> pipesConfiguration = configuration.getPipesConfiguration();

    PipeConfiguration pipeConfiguration = pipesConfiguration.get(0);

    Assert.assertEquals("logEventStream", pipeConfiguration.getSinkConfiguration().getStreamName());

    Assert.assertEquals(60000, pipeConfiguration.getSourceConfiguration().getFailureSleepInterval());
  }

  @Test(expected = ConfigurationLoaderException.class)
  public void failureTest() throws ConfigurationLoadingException {

    ConfigurationLoader loader = new ConfigurationLoaderImpl();

    String path = getClass().getClassLoader().getResource("test.properties").getFile();

    Configuration configuration = loader.load(new File(path));

    List<PipeConfiguration> pipesConfiguration = configuration.getPipesConfiguration();

    PipeConfiguration pipeConfiguration = pipesConfiguration.get(0);

    pipeConfiguration.getSourceConfiguration().getFileName();
  }
}
