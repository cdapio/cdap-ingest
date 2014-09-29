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
   * Retrieves the directory, loaded from the configuration file, for the storage of File Tailer state and metrics.
   *
   * @return the File Tailer home directory for saved state and metrics
   */
  File getDaemonDir();

  /**
   * Retrieves the state file name loaded from the configuration file.
   *
   * @return the state file name
   */
  String getStateFile();

  /**
   * Retrieves the statistics file name loaded from the configuration file.
   *
   * @return the statistics file name
   */
  String getStatisticsFile();

  /**
   * Retrieves the sleep interval for writing statistics, loaded from the configuration file.
   *
   * @return the sleep interval for writing statistics
   */
  long getStatisticsSleepInterval();

  /**
   * Retrieves the size of the queue used for storing events before pushing them to the Stream,
   * as loaded from the configuration file.
   *
   * @return the size of the temporary events queue
   */
  int getQueueSize();

  /**
   * Retrieves the source configuration loader of this pipe.
   *
   * @return the source configuration loader of this pipe
   */
  SourceConfiguration getSourceConfiguration();

  /**
   * Retrieves the sink configuration loader of this pipe.
   *
   * @return the sink configuration loader of this pipe
   */
  SinkConfiguration getSinkConfiguration();
}
