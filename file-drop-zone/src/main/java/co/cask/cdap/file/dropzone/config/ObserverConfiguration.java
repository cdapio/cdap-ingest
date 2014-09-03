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

package co.cask.cdap.file.dropzone.config;

import co.cask.cdap.filetailer.config.PipeConfiguration;

/**
 * ObserverConfiguration is design for getting properties of the some observer
 */
public interface ObserverConfiguration {

  /**
   * Returns the name of this observer
   *
   * @return the name of this observer
   */
  String getName();

  /**
   * Returns the path to directory, intended like storage for File DropZone state and metrics
   * (loaded from configuration file)
   *
   * @return the File DropZone home (to save state and metrics) directory path
   */
  String getDaemonDir();

  /**
   * Returns the pipe configuration of this observer
   *
   * @return the pipe configuration of this observer
   */
  PipeConfiguration getPipeConf();
}
