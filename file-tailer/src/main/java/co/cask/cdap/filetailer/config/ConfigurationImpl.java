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

import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration interface implementation
 */
public class ConfigurationImpl implements Configuration {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationImpl.class);

  private static final String DEFAULT_POLLING_INTERVAL = "5000";

  private static final String DEFAULT_WORK_DIR = "var/file-drop-zone/";

  private Properties properties;

  public ConfigurationImpl(Properties properties) {
    this.properties = properties;
  }

  @Override
  public List<PipeConfiguration> getPipesConfiguration() {
    String[] pipes = getRequiredProperty("pipes").split(",");
    List<PipeConfiguration> pipesConfiguration = new ArrayList<PipeConfiguration>(pipes.length);
    for (String pipe : pipes) {
      pipesConfiguration.add(new PipeConfigurationImpl(properties, pipe));
    }
    return pipesConfiguration;
  }

  @Override
  public long getPollingInterval() {
    return Long.parseLong(getProperty("polling_interval", DEFAULT_POLLING_INTERVAL));
  }

  @Override
  public List<PipeConfiguration> getObserverConfiguration() {
    String[] observers = getRequiredProperty("observers").split(",");
    List<PipeConfiguration> pipesConfiguration = new ArrayList<PipeConfiguration>(observers.length);
    for (String observer : observers) {
      String pipe = getRequiredProperty("observers." + observer + ".pipe");
      Properties newProperties = new Properties();
      newProperties.putAll(properties);
      newProperties.put("pipes." + pipe + ".source.work_dir", DEFAULT_WORK_DIR + observer);
      pipesConfiguration.add(new PipeConfigurationImpl(newProperties, pipe));
    }
    return pipesConfiguration;
  }

  private String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return value != null && !value.equals("") ? value : defaultValue;
  }

  private String getProperty(String key) {
    LOG.debug("Start returning property by key: {}", key);
    if (properties == null) {
      LOG.error("Properties file not loaded");
      throw new ConfigurationLoaderException("Properties file not loaded");
    }
    return properties.getProperty(key);
  }

  private String getRequiredProperty(String key) {
    String property = getProperty(key);
    if (property == null) {
      LOG.error("Property {} not found", key);
      throw new ConfigurationLoaderException("Property " + key + " not found");
    }
    return property;
  }
}
