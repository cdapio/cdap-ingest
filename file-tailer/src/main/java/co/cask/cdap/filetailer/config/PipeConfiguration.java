/*
 * Copyright © 2014 Cask Data, Inc.
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

import java.io.File;

/**
 * Retrieves general properties of a pipe.
 */
public interface PipeConfiguration {

  /**
   * Retrieves the name of the pipe that was loaded from the configuration file.
   *
   * @return the name of this pipe
   */
  String getPipeName();

  /**
   * Returns the directory, intended like storage for File Tailer state and metrics
   * (loaded from configuration file)
   *
   * @return the File Tailer home (to save state and metrics) directory
   */
  File getDaemonDir();

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
}
