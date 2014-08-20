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
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getFileName() {
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getRotationPattern() {
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getCharsetName() {
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public byte getRecordSeparator() {
      return FlowConfigurationImpl.this.getProperty(this.key + "record_separator").getBytes()[0];
    }

    @Override
    public long getSleepInterval() {
      return Long.parseLong(FlowConfigurationImpl.this.getProperty(this.key + "sleep_interval"));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(FlowConfigurationImpl.this.getProperty(this.key + "failure_retry_limit"));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(FlowConfigurationImpl.this.getProperty(this.key + "failure_sleep_interval"));
    }
  }

  private class SinkConfigurationImpl implements SinkConfiguration {

    private String key;

    public SinkConfigurationImpl(String key) {
      this.key = "flows." + key + ".sink.";
    }

    @Override
    public String getStreamName() {
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "stream_name");
    }

    @Override
    public String getHost() {
      return FlowConfigurationImpl.this.getRequiredProperty(this.key + "host");
    }

    @Override
    public int getPort() {
      return Integer.parseInt(FlowConfigurationImpl.this.getRequiredProperty(this.key + "port"));
    }

    @Override
    public boolean getSSL() {
      return Boolean.valueOf(FlowConfigurationImpl.this.getProperty(this.key + "ssl"));
    }

    @Override
    public String getAuthToken() {
      return FlowConfigurationImpl.this.getProperty(this.key + "authToken");
    }

    @Override
    public String getApiKey() {
      return FlowConfigurationImpl.this.getProperty(this.key + "apiKey");
    }

    @Override
    public int getWriterPoolSize() {
      return Integer.parseInt(FlowConfigurationImpl.this.getProperty(this.key + "writerPoolSize"));
    }

    @Override
    public String getVersion() {
      return FlowConfigurationImpl.this.getProperty(this.key + "version");
    }

    @Override
    public int getPackSize() {
      return Integer.parseInt(FlowConfigurationImpl.this.getProperty(this.key + "packSize"));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(FlowConfigurationImpl.this.getProperty(this.key + "failure_retry_limit"));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(FlowConfigurationImpl.this.getProperty(this.key + "failure_sleep_interval"));
    }
  }
}
