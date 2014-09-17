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

package co.cask.cdap.file.dropzone;

import co.cask.cdap.file.dropzone.polling.PollingServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown polling service
 */
class FileDropZoneShutdownTask implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(FileDropZoneShutdownTask.class);

  private PollingServiceManager manager;

  FileDropZoneShutdownTask(PollingServiceManager manager) {
    this.manager = manager;
  }

  @Override
  public void run() {
    LOG.info("Kill signal received, trying to shutdown drop zone gracefully");
    try {
      manager.stopMonitor();
    } catch (Exception e) {
      LOG.error("Cannot stop polling service: {}", e);
    }

  }
}
