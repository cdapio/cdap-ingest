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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObserverConfiguration default implementation
 */
public class ObserverConfigurationImpl implements ObserverConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(ObserverConfigurationImpl.class);

  private String name;

  private PipeConfiguration pipeConfiguration;

  public ObserverConfigurationImpl(String name, PipeConfiguration pipeConfiguration) {
    this.name = name;
    this.pipeConfiguration = pipeConfiguration;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDaemonDir() {
    return pipeConfiguration.getDaemonDir().replace("pipe", "observer");
  }

  @Override
  public PipeConfiguration getPipeConf() {
    return pipeConfiguration;
  }
}
