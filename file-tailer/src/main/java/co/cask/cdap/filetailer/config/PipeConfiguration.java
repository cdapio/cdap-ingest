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

/**
 * PipeConfiguration is design for getting general properties of the some pipe
 */
public interface PipeConfiguration {

  /**
   * Returns the name of this pipe (loaded from configuration file)
   *
   * @return the name of this pipe
   */
  String getPipeName();

  /**
   * Returns the path to directory, intended like storage for File Tailer state and metrics
   * (loaded from configuration file)
   *
   * @return the File Tailer home (to save state and metrics) directory path
   */
  String getDaemonDir();

  /**
   * Returns the name of state file (loaded from configuration file)
   *
   * @return the state file name
   */
  String getStateFile();

  /**
   * Returns the name of statistics file (loaded from configuration file)
   *
   * @return the state file name
   */
  String getStatisticsFile();

  /**
   * Returns the sleep interval for writing statistics (loaded from configuration file)
   *
   * @return the sleep interval for writing statistics
   */
  long getStatisticsSleepInterval();

  /**
   * Returns the size of queue, intended for store events before push them to stream (loaded from configuration file)
   *
   * @return the queue size of temporary events queue
   */
  int getQueueSize();

  /**
   * Returns the source configuration loader that return source configurations of this pipe
   *
   * @return the source configuration loader of this pipe
   */
  SourceConfiguration getSourceConfiguration();

  /**
   * Returns the sink configuration loader that return sink configurations of this pipe
   *
   * @return the sink configuration loader of this pipe
   */
  SinkConfiguration getSinkConfiguration();

  /**
   * Returns the configuration of pipe, that working with specified file
   *
   * @param fileName the name of work file
   * @return the configuration of pipe, that working with specified file
   */
  public PipeConfiguration getPipeConfiguration(String fileName);
}
