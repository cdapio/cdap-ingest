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

package co.cask.cdap.filetailer.config;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * PipeConfigurationImpl default implementation of PipeConfiguration
 */
public class PipeConfigurationImpl implements PipeConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(PipeConfigurationImpl.class);
  private static final String DEFAULT_DAEMON_DIR = "/var/run/file-tailer/state_dir";
  private static final String DEFAULT_QUEUE_SIZE = "1000";
  private static final String DEFAULT_STATISTICS_SLEEP_INTERVAL = "60000";
  private static final String DEFAULT_STATE_FILE = "state";
  private static final String DEFAULT_STATISTICS_FILE = "stats";
  private static final String DEFAULT_AUTH_CLIENT = BasicAuthenticationClient.class.getName();

  private final Properties properties;
  private final String key;
  private final String keyPath;
  private final SourceConfiguration sourceConfiguration;
  private final SinkConfiguration sinkConfiguration;

  public PipeConfigurationImpl(Properties properties, String key) {
    this.properties = properties;
    this.key = key;
    this.keyPath = "pipes." + key + ".";
    sourceConfiguration = new SourceConfigurationImpl(key);
    sinkConfiguration = new SinkConfigurationImpl(key);
  }

  @Override
  public String getPipeName() {
    return getProperty(this.keyPath + "name", key);
  }

  @Override
  public File getDaemonDir() {
    return new File(getProperty("daemon_dir", DEFAULT_DAEMON_DIR),
                    keyPath.substring(0, keyPath.length() - 1).replace('.', '/'));
  }

  @Override
  public String getStateFile() {
    return getProperty(this.keyPath + "state_file", DEFAULT_STATE_FILE);
  }

  @Override
  public String getStatisticsFile() {
    return getProperty(this.keyPath + "statistics_file", DEFAULT_STATISTICS_FILE);
  }

  @Override
  public long getStatisticsSleepInterval() {
    return Long.parseLong(getProperty(this.keyPath + "statistics_sleep_interval", DEFAULT_STATISTICS_SLEEP_INTERVAL));
  }

  @Override
  public int getQueueSize() {
    return Integer.parseInt(getProperty(this.keyPath + "queue_size", DEFAULT_QUEUE_SIZE));
  }

  @Override
  public SourceConfiguration getSourceConfiguration() {
    return sourceConfiguration;
  }

  @Override
  public SinkConfiguration getSinkConfiguration() {
    return sinkConfiguration;
  }

  private String getProperty(String key, String defaultValue) {
      String value = getProperty(key);
      return value != null && !value.equals("") ? value : defaultValue;
  }

  private String getProperty(String key) {
    LOG.debug("Start returning property by keyPath: {}", key);
    if (properties == null) {
      LOG.error("Properties file not loaded");
      throw new ConfigurationLoaderException("Properties file not loaded");
    }
    return properties.getProperty(key);
  }

  private String getRequiredProperty(String key) {
    String property = getProperty(key);
    if (property == null || property.equals("")) {
      LOG.error("Property {} not found", key);
      throw new ConfigurationLoaderException("Property " + key + " not found");
    }
    return property;
  }

  private class SourceConfigurationImpl implements SourceConfiguration {

    private static final String DEFAULT_ROTATED_FILE_NAME_PATTERN = "(.*)";
    private static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final String DEFAULT_RECORD_SEPARATOR = "\n";
    private static final String DEFAULT_SLEEP_INTERVAL = "3000";
    private static final String DEFAULT_FAILURE_RETRY_LIMIT = "0";
    private static final String DEFAULT_FAILURE_SLEEP_INTERVAL = "60000";

    private final String key;

    public SourceConfigurationImpl(String key) {
      this.key = "pipes." + key + ".source.";
    }

    @Override
    public File getWorkDir() {
      return new File(getRequiredProperty(this.key + "work_dir"));
    }

    @Override
    public String getFileName() {
      return getRequiredProperty(this.key + "file_name");
    }

    @Override
    public String getRotationPattern() {
      return getProperty(this.key + "rotated_file_name_pattern", getFileName() + DEFAULT_ROTATED_FILE_NAME_PATTERN);
    }

    @Override
    public String getCharsetName() {
      return getProperty(this.key + "charset_name", DEFAULT_CHARSET_NAME);
    }

    @Override
    public char getRecordSeparator() {
      return getProperty(this.key + "record_separator", DEFAULT_RECORD_SEPARATOR).charAt(0);
    }

    @Override
    public long getSleepInterval() {
      return Long.parseLong(getProperty(this.key + "sleep_interval", DEFAULT_SLEEP_INTERVAL));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(getProperty(this.key + "failure_retry_limit", DEFAULT_FAILURE_RETRY_LIMIT));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(getProperty(this.key + "failure_sleep_interval", DEFAULT_FAILURE_SLEEP_INTERVAL));
    }
  }

  private class SinkConfigurationImpl implements SinkConfiguration {

    private static final String DEFAULT_SSL = "false";
    private static final String DEFAULT_WRITER_POOL_SIZE = "10";
    private static final String DEFAULT_VERSION = "v2";
    private static final String DEFAULT_PACK_SIZE = "1";
    private static final String DEFAULT_FAILURE_RETRY_LIMIT = "0";
    private static final String DEFAULT_FAILURE_SLEEP_INTERVAL = "60000";
    private static final String DEFAULT_AUTH_CLIENT_PROPERTIES = "/etc/file-tailer/conf/auth-client.properties";

    private final String key;

    public SinkConfigurationImpl(String key) {
      this.key = "pipes." + key + ".sink.";
    }

    @Override
    public String getStreamName() {
      return getRequiredProperty(this.key + "stream_name");
    }

    @Override
    public StreamClient getStreamClient() {
      String host = getRequiredProperty(this.key + "host");
      int port = Integer.parseInt(getRequiredProperty(this.key + "port"));
      boolean ssl = Boolean.valueOf(getProperty(this.key + "ssl", DEFAULT_SSL));

      RestStreamClient.Builder builder = RestStreamClient.builder(host, port).ssl(ssl);

      String authClientClassPath = getProperty(this.key + "auth_client", DEFAULT_AUTH_CLIENT);
      String authClientPropertiesPath = getProperty(this.key + "auth_client_properties",
                                                    DEFAULT_AUTH_CLIENT_PROPERTIES);
      try {
        AuthenticationClient authClient =
          (AuthenticationClient) Class.forName(authClientClassPath).getConstructor().newInstance();
        authClient.setConnectionInfo(host, port, ssl);
        authClient.configure(new ConfigurationLoaderImpl().load(new File(authClientPropertiesPath)).getProperties());
        builder.authClient(authClient);
      } catch (ReflectiveOperationException e) {
        LOG.error("Can not resolve class {}: {}", authClientClassPath, e.getMessage(), e);
      } catch (ConfigurationLoadingException e) {
        LOG.error("Can not load Authentication Client properties file {}: {}",
                  authClientPropertiesPath, e.getMessage(), e);
      }

      String apiKey = getProperty(this.key + "apiKey");
      if (apiKey != null && !apiKey.equals("")) {
        builder.apiKey(apiKey);
      }

      builder.writerPoolSize(Integer.parseInt(getProperty(this.key + "writerPoolSize", DEFAULT_WRITER_POOL_SIZE)));

      builder.version(getProperty(this.key + "version", DEFAULT_VERSION));

      return builder.build();
    }

    @Override
    public int getPackSize() {
      return Integer.parseInt(getProperty(this.key + "packSize", DEFAULT_PACK_SIZE));
    }

    @Override
    public int getFailureRetryLimit() {
      return Integer.parseInt(getProperty(this.key + "failure_retry_limit", DEFAULT_FAILURE_RETRY_LIMIT));
    }

    @Override
    public long getFailureSleepInterval() {
      return Long.parseLong(getProperty(this.key + "failure_sleep_interval", DEFAULT_FAILURE_SLEEP_INTERVAL));
    }
  }
}
