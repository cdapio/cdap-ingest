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

  private static final String DEFAULT_STATISTICS_SLEEP_INTERVAL = "60000";

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
  public long getStatisticsSleepInterval() {
    String statisticsSleepInterval = getProperty(this.key + "statistics_sleep_interval");
    return Long.parseLong(statisticsSleepInterval != null ?
                            statisticsSleepInterval : DEFAULT_STATISTICS_SLEEP_INTERVAL);
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

    private static final String DEFAULT_RECORD_SEPARATOR = "\n";

    private static final String DEFAULT_SLEEP_INTERVAL = "3000";

    private static final String DEFAULT_FAILURE_RETRY_LIMIT = "0";

    private static final String DEFAULT_FAILURE_SLEEP_INTERVAL = "60000";

    public SourceConfigurationImpl(String key) {
      this.key = "flows." + key + ".source.";
    }

    @Override
    public String getWorkDir() {
      return getRequiredProperty(this.key + "work_dir");
    }

    @Override
    public String getFileName() {
      return getRequiredProperty(this.key + "file_name");
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
      String recordSeparator = getProperty(this.key + "record_separator");
      return (recordSeparator != null ? recordSeparator : DEFAULT_RECORD_SEPARATOR).getBytes()[0];
    }

    @Override
    public long getSleepInterval() {
      String sleepInterval = getProperty(this.key + "sleep_interval");
      return Long.parseLong(sleepInterval != null ? sleepInterval : DEFAULT_SLEEP_INTERVAL);
    }

    @Override
    public int getFailureRetryLimit() {
      String failureRetryLimit = getProperty(this.key + "failure_retry_limit");
      return Integer.parseInt(failureRetryLimit != null ? failureRetryLimit : DEFAULT_FAILURE_RETRY_LIMIT);
    }

    @Override
    public long getFailureSleepInterval() {
      String failureSleepInterval = getProperty(this.key + "failure_sleep_interval");
      return Long.parseLong(failureSleepInterval != null ? failureSleepInterval : DEFAULT_FAILURE_SLEEP_INTERVAL);
    }
  }

  private class SinkConfigurationImpl implements SinkConfiguration {

    private String key;

    private static final String DEFAULT_SSL = "false";

    private static final String DEFAULT_WRITER_POOL_SIZE = "10";

    private static final String DEFAULT_VERSION = "v2";

    private static final String DEFAULT_PACK_SIZE = "1";

    private static final String DEFAULT_FAILURE_RETRY_LIMIT = "0";

    private static final String DEFAULT_FAILURE_SLEEP_INTERVAL = "60000";

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
      builder.ssl(Boolean.valueOf(ssl != null ? ssl : DEFAULT_SSL));

      String authToken = getProperty(this.key + "authToken");
      if (authToken != null) {
        builder.authToken(authToken);
      }

      String apiKey = getProperty(this.key + "apiKey");
      if (apiKey != null) {
        builder.apiKey(apiKey);
      }

      String writerPoolSize = getProperty(this.key + "writerPoolSize");
      builder.writerPoolSize(Integer.parseInt(writerPoolSize != null ? writerPoolSize : DEFAULT_WRITER_POOL_SIZE));

      String version = getProperty(this.key + "version");
      builder.version(version != null ? version : DEFAULT_VERSION);

      return builder.build();
    }

    @Override
    public int getPackSize() {
      String packSize = getProperty(this.key + "packSize");
      return Integer.parseInt(packSize != null ? packSize : DEFAULT_PACK_SIZE);
    }

    @Override
    public int getFailureRetryLimit() {
      String failureRetryLimit = getProperty(this.key + "failure_retry_limit");
      return Integer.parseInt(failureRetryLimit != null ? failureRetryLimit : DEFAULT_FAILURE_RETRY_LIMIT);
    }

    @Override
    public long getFailureSleepInterval() {
      String failureSleepInterval = getProperty(this.key + "failure_sleep_interval");
      return Long.parseLong(failureSleepInterval != null ? failureSleepInterval : DEFAULT_FAILURE_SLEEP_INTERVAL);
    }
  }
}
