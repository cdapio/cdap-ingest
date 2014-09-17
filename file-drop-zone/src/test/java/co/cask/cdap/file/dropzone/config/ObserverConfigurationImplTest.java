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

package co.cask.cdap.file.dropzone.config;

import co.cask.cdap.filetailer.config.PipeConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

/**
 * ObserverConfigurationImpl test class
 */
public class ObserverConfigurationImplTest {

  private static final String OLD_DAEMON_DIR = "/var/run/pipe/test/pipes/pipe11";
  private static final String NEW_DAEMON_DIR = "/var/run/pipe/test/observers/obs2";

  @Test
  public void getDaemonDirTest() throws NoSuchFieldException, IllegalAccessException {
    Properties properties = Mockito.mock(Properties.class);
    PipeConfiguration pipeConfiguration = Mockito.mock(PipeConfiguration.class);

    Mockito.when(pipeConfiguration.getDaemonDir()).thenReturn(new File(OLD_DAEMON_DIR));

    ObserverConfiguration observerConfiguration = new ObserverConfigurationImpl("obs2", properties, "key");
    Field field = observerConfiguration.getClass().getDeclaredField("pipeConfiguration");
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(observerConfiguration, pipeConfiguration);

    Assert.assertEquals(new File(NEW_DAEMON_DIR), observerConfiguration.getDaemonDir());
  }
}
