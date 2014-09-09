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

import co.cask.cdap.filetailer.config.ConfigurationImpl;
import co.cask.cdap.filetailer.config.PipeConfigurationImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * FileDropZoneConfiguration default implementation
 */
public class FileDropZoneConfigurationImpl extends ConfigurationImpl implements FileDropZoneConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(FileDropZoneConfigurationImpl.class);
  private static final String DEFAULT_POLLING_INTERVAL = "5000";
  private static final String DEFAULT_WORK_DIR = "/var/file-drop-zone/";

  public FileDropZoneConfigurationImpl(Properties properties) {
    super(properties);
  }

  @Override
  public long getPollingInterval() {
    return Long.parseLong(getProperty("polling_interval", DEFAULT_POLLING_INTERVAL));
  }

  @Override
  public List<ObserverConfiguration> getObserverConfiguration() {
    String[] observers = getRequiredProperty("observers").split(",");
    List<ObserverConfiguration> observersConfiguration = new ArrayList<ObserverConfiguration>(observers.length);
    for (String observer : observers) {
      String pipe = getRequiredProperty("observers." + observer + ".pipe");
      Properties newProperties = new Properties();
      newProperties.putAll(getProperties());
      newProperties.put("pipes." + pipe + ".source.work_dir", getWorkDir() + observer);
      newProperties.put("pipes." + pipe + ".source.read_rotated_files", "false");
      observersConfiguration.add(new ObserverConfigurationImpl(observer,
                                                               new PipeConfigurationImpl(newProperties, pipe)));
    }
    return observersConfiguration;
  }

  /**
   * Return the path to work directory
   *
   * @return the path to directory where polling dirs located
   */
  private String getWorkDir() {
    String workDir = getProperty("work_dir", DEFAULT_WORK_DIR);
    if (!workDir.endsWith("/")) {
      workDir += "/";
    }
    LOG.info("work directory = {}", workDir);
    return workDir;
  }

  /**
   * Return property value
   *
   * @param key The property key
   * @param defaultValue The default value of property
   * @return property value
   */
  private String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return value != null && !value.trim().equals("") ? value : defaultValue;
  }

  /**
   * Return property value
   *
   * @param key The property key
   * @return property value
   */
  private String getProperty(String key) {
    LOG.debug("Start returning property by key: {}", key);
    if (getProperties() == null) {
      LOG.error("Properties file not loaded");
      throw new ConfigurationLoaderException("Properties file not loaded");
    }
    return getProperties().getProperty(key);
  }

  /**
   * Return property value
   *
   * @param key The property key
   * @return property value
   * @throws ConfigurationLoaderException if properties not found
   */
  private String getRequiredProperty(String key) {
    String property = getProperty(key);
    if (property == null) {
      LOG.error("Property {} not found", key);
      throw new ConfigurationLoaderException("Property " + key + " not found");
    }
    return property;
  }
}
