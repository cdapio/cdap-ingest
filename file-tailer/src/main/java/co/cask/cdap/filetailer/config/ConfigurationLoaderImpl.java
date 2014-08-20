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
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ConfigurationLoader interface implementation
 */
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoaderImpl.class);

  private Properties properties;

  @Override
  public void load(String path) throws ConfigurationLoadingException {
    LOG.debug("Start initializing loader with file: {}", path);
    properties = new Properties();
    try {
      properties.load(new FileInputStream(path));
      LOG.debug("Loader successfully initialized with file: {}", path);
    } catch (IOException e) {
      LOG.error("Can not load properties: {}", e.getMessage());
      throw new ConfigurationLoadingException(e.getMessage());
    }
  }

  @Override
  public List<StreamClient> getStreamClients() {
    List<StreamClient> streamClients = new ArrayList<StreamClient>();
    int clientCounter = 1;
    String clientHost;
    String clientPort;
    while ((clientHost = getProperty("client" + clientCounter + ".host")) != null &&
           (clientPort = getProperty("client" + clientCounter + ".port")) != null) {
      RestStreamClient.Builder builder = new RestStreamClient.Builder(clientHost, Integer.parseInt(clientPort));
      String ssl = getProperty("client" + clientCounter + ".ssl");
      if (ssl != null) {
        builder.ssl(Boolean.valueOf(ssl));
      }
      String authToken = getProperty("client" + clientCounter + ".authToken");
      if (authToken != null) {
        builder.authToken(authToken);
      }
      String apiKey = getProperty("client" + clientCounter + ".apiKey");
      if (apiKey != null) {
        builder.apiKey(apiKey);
      }
      String writerPoolSize = getProperty("client" + clientCounter + ".writerPoolSize");
      if (writerPoolSize != null) {
        builder.writerPoolSize(Integer.parseInt(writerPoolSize));
      }
      String version = getProperty("client" + clientCounter + ".version");
      if (version != null) {
        builder.version(version);
      }
      streamClients.add(builder.build());

      clientCounter++;
    }
    if (streamClients.isEmpty()) {
      throw new ConfigurationLoaderException("Not found any stream client in configuration file");
    }
    return streamClients;
  }

  @Override
  public String getStreamName() {
    return getRequiredProperty("stream_name");
  }

  @Override
  public String getCharsetName() {
    return getRequiredProperty("charset_name");
  }

  @Override
  public String getSinkStrategy() {
    return getRequiredProperty("sink_strategy");
  }

  @Override
  public String getWorkDir() {
    return getRequiredProperty("work_dir");
  }

  @Override
  public String getFileName() {
    return getRequiredProperty("file_name");
  }

  @Override
  public String getRotationPattern() {
    return getRequiredProperty("rotated_file_pattern");
  }

  @Override
  public String getStateDir() {
    return getRequiredProperty("state_dir");
  }

  @Override
  public String getStateFile() {
    return getRequiredProperty("state_file");
  }

  @Override
  public int getFailureRetryLimit() {
    return Integer.parseInt(getRequiredProperty("failure_retry_limit"));
  }

  @Override
  public byte getRecordSeparator() {
    return getRequiredProperty("record_separator").getBytes()[0];
  }

  @Override
  public long getSleepInterval() {
    return Long.parseLong(getRequiredProperty("sleep_interval"));
  }

  @Override
  public int getQueueSize() {
    return Integer.parseInt(getRequiredProperty("queue_size"));
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
      LOG.error("Property not found");
      throw new ConfigurationLoaderException("Property not found");
    }
    return property;
  }
}
