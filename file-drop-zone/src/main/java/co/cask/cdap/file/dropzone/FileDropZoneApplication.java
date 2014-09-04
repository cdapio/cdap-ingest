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

package co.cask.cdap.file.dropzone;

import co.cask.cdap.file.dropzone.polling.PollingServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An Example of using the Directory Polling Service
 */
public class FileDropZoneApplication {
  private static final Logger LOG = LoggerFactory.getLogger(FileDropZoneApplication.class);

  public static void main(String[] args) throws Exception {

    LOG.info("Application started");

    String configurationPath;
    if (args.length == 0) {
      configurationPath =
        FileDropZoneApplication.class.getClassLoader().getResource("file-drop-zone.properties").getFile();
    } else if (args.length == 1) {
      configurationPath = args[0];
    } else {
      LOG.error("Too many arguments: {}", args.length);
      return;
    }

    PollingServiceManager pollingServiceManager = new PollingServiceManager(configurationPath);
    try {
      pollingServiceManager.initManager();
      pollingServiceManager.initObservers();
    } catch (IOException e) {
      LOG.error("Failed to initialize observers");
      return;
    }
    LOG.info("Starting monitor");
    pollingServiceManager.startMonitor();
    Runtime.getRuntime().addShutdownHook(new Thread(new FileDropZoneShutdownTask(pollingServiceManager)));
  }
}
