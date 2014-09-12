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
 * Retrieves source properties of a pipe.
 */
public interface SourceConfiguration {

  /**
   * Retrieves the work directory (directory with logs) for File Tailer (loaded from configuration file)
   *
   * @return the work directory
   */
  File getWorkDir();

  /**
   * Retrieves the name of work file (loaded from configuration file)
   *
   * @return the work file name
   */
  String getFileName();

  /**
   * Retrieves the pattern of rotates files (loaded from configuration file)
   *
   * @return the rotated files pattern
   */
  String getRotationPattern();

  /**
   * Retrieves the charset name (loaded from configuration file)
   *
   * @return the charset name
   */
  String getCharsetName();

  /**
   * Retrieves the separator between every record (loaded from configuration file)
   *
   * @return the separator between records
   */
  char getRecordSeparator();

  /**
   * Retrieves the interval for File Tailer to wait, after read all data of new file (loaded from configuration file)
   *
   * @return the sleep interval for File Tailer
   */
  long getSleepInterval();

  /**
   * Retrieves the failure retry limit (limit for the number of attempts to read record, if error occurred)
   * (loaded from configuration file)
   *
   * @return the failure retry limit
   */
  int getFailureRetryLimit();

  /**
   * Retrieves the interval for File Tailer to wait, after occurred error while reading file data
   * (loaded from configuration file)
   *
   * @return the failure sleep interval for File Tailer
   */
  long getFailureSleepInterval();
}
