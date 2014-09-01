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

package co.cask.cdap.file.dropzone.polling.config;

import co.cask.cdap.filetailer.config.PipeConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * ObserverConfigurationImpl test class
 */
public class ObserverConfigurationImplTest {

  private static final String OLD_DAEMON_DIR = "/var/run/pipe/test/pipes/pipe11";
  private static final String NEW_DAEMON_DIR = "/var/run/pipe/test/observers/obs2";

  @Test
  public void getDaemonDirTest() {
    PipeConfiguration pipeConfiguration = Mockito.mock(PipeConfiguration.class);

    Mockito.when(pipeConfiguration.getDaemonDir()).thenReturn(OLD_DAEMON_DIR);

    ObserverConfiguration observerConfiguration = new ObserverConfigurationImpl("obs2", pipeConfiguration);
    Assert.assertEquals(NEW_DAEMON_DIR, observerConfiguration.getDaemonDir());
  }
}
