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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * ConfigurationLoader interface implementation
 */
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoaderImpl.class);

  private Properties properties;

  @Override
  public void load(String path) throws ConfigurationLoaderException {
    LOG.debug("Start initializing loader with file: {}", path);
    properties = new Properties();
    try {
      properties.load(new FileInputStream(path));
      LOG.debug("Loader successfully initialized with file: {}", path);
    } catch (IOException e) {
      LOG.error("Can not load properties: {}", e.getMessage());
      throw new ConfigurationLoaderException(e.getMessage());
    }
  }

  @Override
  public List<String> getHostPortPairs() throws ConfigurationLoaderException {
    return Arrays.asList(getProperty("rest_api_hosts").split(";"));
  }

  @Override
  public String getStreamName() throws ConfigurationLoaderException {
    return getProperty("stream_name");
  }

  @Override
  public String getCharsetName() throws ConfigurationLoaderException {
    return getProperty("charset_name");
  }

  @Override
  public String getSinkStrategy() throws ConfigurationLoaderException {
    return getProperty("sink_strategy");
  }

  @Override
  public String getWorkDir() throws ConfigurationLoaderException {
    return getProperty("work_dir");
  }

  @Override
  public String getFileName() throws ConfigurationLoaderException {
    return getProperty("file_name");
  }

  @Override
  public String getRotationPattern() throws ConfigurationLoaderException {
    return getProperty("rotated_file_pattern");
  }

  @Override
  public String getStateDir() throws ConfigurationLoaderException {
    return getProperty("state_dir");
  }

  @Override
  public String getStateFile() throws ConfigurationLoaderException {
    return getProperty("state_file");
  }

  @Override
  public int getFailureRetryLimit() throws ConfigurationLoaderException {
    return Integer.parseInt(getProperty("failure_retry_limit"));
  }

  @Override
  public byte getRecordSeparator() throws ConfigurationLoaderException {
    return getProperty("record_separator").getBytes()[0];
  }

  @Override
  public long getSleepInterval() throws ConfigurationLoaderException {
    return Long.parseLong(getProperty("sleep_interval"));
  }

  @Override
  public int getQueueSize() throws ConfigurationLoaderException {
    return Integer.parseInt(getProperty("queue_size"));
  }

  private String getProperty(String key) throws ConfigurationLoaderException {
    LOG.debug("Start returning property by key: {}", key);
    if (properties == null) {
      LOG.error("Properties file not loaded");
      throw new ConfigurationLoaderException("Properties file not loaded");
    }
    String property = properties.getProperty(key);
    if (property == null) {
      LOG.error("Property not found");
      throw new ConfigurationLoaderException("Property not found");
    }
    return property;
  }
}
