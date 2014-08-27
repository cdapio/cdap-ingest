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

package co.cask.cdap.file.dropzone;

import co.cask.cdap.file.dropzone.polling.PollingListener;
import co.cask.cdap.file.dropzone.polling.PollingService;
import co.cask.cdap.file.dropzone.polling.dir.DirPollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * An Example of using the Directory Polling Service
 */
public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  // A hardcoded path to a folder you are monitoring .
  public static final String FOLDER = "/tmp/dropzone";


  public static void main(String[] args) throws Exception {
    final long pollingInterval = 5 * 1000;

    final File folder = new File(FOLDER);

    if (!folder.exists()) {
      // Test to see if monitored folder exists
      throw new RuntimeException("Directory not found: " + FOLDER);
    }

    final PollingService monitor = new DirPollingService(pollingInterval);
    monitor.startDirMonitor(folder, new PollingListener() {

      @Override
      public void onFileCreate(File file) {
        LOG.debug("File Added: {}", file.getAbsolutePath());
        monitor.removeFile(folder, file);
      }

      @Override
      public void onException(Exception exception) {
        LOG.warn("Error", exception);
        monitor.stopDirMonitor(folder);
      }
    });

    monitor.start();
  }
}
