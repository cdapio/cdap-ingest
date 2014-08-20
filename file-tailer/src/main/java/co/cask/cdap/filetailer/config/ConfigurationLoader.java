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
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;

import java.util.List;

/**
 * ConfigurationLoader is designed for loading properties from specified file.
 */
public interface ConfigurationLoader {

  /**
   * Initialize configuration loader with properties from configuration file.
   *
   * @param path the path to configuration file
   * @throws co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException if error occurred
   *                                      (for example, file not exists)
   */
  void load(String path) throws ConfigurationLoadingException;

  /**
   * Returns the List of stream clients (loaded from configuration file)
   *
   * @return the List of stream clients
   */
  List<StreamClient> getStreamClients();

  /**
   * Returns the name of target stream (loaded from configuration file)
   *
   * @return the target stream name
   */
  String getStreamName();

  /**
   * Returns the charset name (loaded from configuration file)
   *
   * @return the charset name
   */
  String getCharsetName();

  /**
   * Returns the sink strategy (loaded from configuration file)
   *
   * @return the sink strategy (failover or load_balance)
   */
  String getSinkStrategy();

  /**
   * Returns the path to work directory (directory with logs) for File Tailer (loaded from configuration file)
   *
   * @return the work directory path
   */
  String getWorkDir();

  /**
   * Returns the name of work file (loaded from configuration file)
   *
   * @return the work file name
   */
  String getFileName();

  /**
   * Returns the pattern of rotates files (loaded from configuration file)
   *
   * @return the rotated files pattern
   */
  String getRotationPattern();

  /**
   * Returns the path of directory, intended like storage for File Tailer state and metrics
   * (loaded from configuration file)
   *
   * @return the File Tailer home (to save state and metrics) directory path
   */
  String getStateDir();

  /**
   * Returns the name of state file (loaded from configuration file)
   *
   * @return the state file name
   */
  String getStateFile();

  /**
   * Returns the failure retry limit (limit for the number of attempts to read record, if error occurred)
   * (loaded from configuration file)
   *
   * @return the failure retry limit
   */
  int getFailureRetryLimit();

  /**
   * Returns the separator between every record (loaded from configuration file)
   *
   * @return the separator between records
   */
  byte getRecordSeparator();

  /**
   * Returns the interval for File Tailer to wait, after read all data of new file (loaded from configuration file)
   *
   * @return the sleep interval for File Tailer
   */
  long getSleepInterval();

  /**
   * Returns the size of queue, intended for store events before push them to server (loaded from configuration file)
   *
   * @return the queue size of temporary events queue
   */
  int getQueueSize();
}
