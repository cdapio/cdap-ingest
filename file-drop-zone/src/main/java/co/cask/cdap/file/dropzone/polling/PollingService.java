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

package co.cask.cdap.file.dropzone.polling;

import java.io.File;

/**
 * The Polling Service interface to monitor predefined dirs .
 */
public interface PollingService {

  /**
   * Start monitoring.
   *
   * @throws Exception if an error occurs initializing the observer
   */
  void start() throws Exception;

  /**
   * Stop monitoring.
   *
   * @throws Exception if an error occurs initializing the observer
   */
  void stop() throws Exception;

  /**
   * Register new Observer to start monitor specified directory
   *
   * @param dir the observed directory
   * @param listener the listener for listen create new file event
   */
  void registerDirMonitor(File dir, PollingListener listener);

  /**
   * Remove processed File from the observed directory
   *
   * @param folder the observed directory
   * @param file the processed File to remove
   */
  void removeFile(File folder, File file);
}
