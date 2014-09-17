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
import co.cask.cdap.filetailer.config.PipeConfigurationImpl;

import java.io.File;
import java.util.Properties;

/**
 * ObserverConfiguration default implementation
 */
public class ObserverConfigurationImpl implements ObserverConfiguration {

  private final String name;
  private final String key;
  private final String keyPath;
  private final Properties properties;
  private final PipeConfiguration pipeConfiguration;

  public ObserverConfigurationImpl(String name, Properties properties, String key) {
    this.name = name;
    this.key = key;
    this.keyPath = "pipes." + key + ".";
    this.properties = properties;
    this.pipeConfiguration = new PipeConfigurationImpl(properties, key);
  }

  @Override
  public PipeConfiguration getPipeConfiguration(String fileName) {
    Properties newProperties = new Properties();
    newProperties.putAll(properties);
    newProperties.put(keyPath + "source.file_name", fileName);
    return new PipeConfigurationImpl(newProperties, key);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public File getDaemonDir() {
    String daemonDirectory = pipeConfiguration.getDaemonDir().getAbsolutePath();
    int observersDirLength = "pipes/".length() + daemonDirectory.length() - daemonDirectory.lastIndexOf("pipe");
    daemonDirectory = daemonDirectory.substring(0, daemonDirectory.length() - observersDirLength);
    return new File(daemonDirectory, "observers/" + name);
  }

  @Override
  public PipeConfiguration getPipeConf() {
    return pipeConfiguration;
  }
}
