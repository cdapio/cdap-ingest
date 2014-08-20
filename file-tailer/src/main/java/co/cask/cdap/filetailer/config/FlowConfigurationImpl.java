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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * FlowConfigurationImpl default implementation of FlowConfiguration
 */
public class FlowConfigurationImpl implements FlowConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(FlowConfigurationImpl.class);

  private Properties properties;

  private String key;

  private SourceConfiguration sourceConfiguration;
  private SinkConfiguration sinkConfiguration;

  public FlowConfigurationImpl(Properties properties, String key) {
    this.properties = properties;
    this.key = "flows." + key + ".";
    sourceConfiguration = new SourceConfigurationImpl(key);
    sinkConfiguration = new SinkConfigurationImpl(key);
  }

  @Override
  public String getFlowName() {
    return getRequiredProperty(this.key + "name");
  }

  @Override
  public String getStateDir() {
    return getRequiredProperty(this.key + "state_dir");
  }

  @Override
  public String getStateFile() {
    return getRequiredProperty(this.key + "state_file");
  }

  @Override
  public String getStatisticsFile() {
    return getRequiredProperty(this.key + "statistics_file");
  }

  @Override
  public int getQueueSize() {
    return Integer.parseInt(getRequiredProperty(this.key + "queue_size"));
  }

  @Override
  public SourceConfiguration getSourceConfiguration() {
    return sourceConfiguration;
  }

  @Override
  public SinkConfiguration getSinkConfiguration() {
    return sinkConfiguration;
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

  private class SourceConfigurationImpl implements SourceConfiguration {

    private String key;

    public SourceConfigurationImpl(String key) {
      this.key = "flows." + key + ".source.";
    }

    @Override
    public String getWorkDir() {
      return getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getFileName() {
      return getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getRotationPattern() {
      return getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getCharsetName() {
      return getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public byte getRecordSeparator() {
      return getProperty(this.key + "record_separator").getBytes()[0];
    }

    @Override
    public long getSleepInterval() {
      return Long.parseLong(getProperty(this.key + "sleep_interval"));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(getProperty(this.key + "failure_retry_limit"));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(getProperty(this.key + "failure_sleep_interval"));
    }
  }

  private class SinkConfigurationImpl implements SinkConfiguration {

    private String key;

    public SinkConfigurationImpl(String key) {
      this.key = "flows." + key + ".sink.";
    }

    @Override
    public String getStreamName() {
      return getRequiredProperty(this.key + "stream_name");
    }

    @Override
    public StreamClient getStreamClient() {
      String host = getRequiredProperty(this.key + "host");
      int port = Integer.parseInt(getRequiredProperty(this.key + "port"));

      RestStreamClient.Builder builder = new RestStreamClient.Builder(host, port);
      String ssl = getProperty(this.key + "ssl");
      if (ssl != null) {
        builder.ssl(Boolean.valueOf(ssl));
      }
      String authToken = getProperty(this.key + "authToken");
      if (authToken != null) {
        builder.authToken(authToken);
      }
      String apiKey = getProperty(this.key + "apiKey");
      if (apiKey != null) {
        builder.apiKey(apiKey);
      }
      String writerPoolSize = getProperty(this.key + "writerPoolSize");
      if (writerPoolSize != null) {
        builder.writerPoolSize(Integer.parseInt(writerPoolSize));
      }
      String version = getProperty(this.key + "version");
      if (version != null) {
        builder.version(version);
      }

      return builder.build();
    }

    @Override
    public int getPackSize() {
      return Integer.parseInt(getProperty(this.key + "packSize"));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(getProperty(this.key + "failure_retry_limit"));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(getProperty(this.key + "failure_sleep_interval"));
    }
  }
}
