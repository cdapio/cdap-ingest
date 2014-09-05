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

import java.io.File;
import java.io.IOException;

/**
 * File DropZone is to support user easily perform bulk ingestion using local files.
 */
public class FileDropZoneMain {
  private static final Logger LOG = LoggerFactory.getLogger(FileDropZoneMain.class);

  public static void main(String[] args) throws Exception {

    LOG.info("Application started");

    File configurationFile;
    if (args.length == 0) {
      configurationFile =
        new File(FileDropZoneMain.class.getClassLoader().getResource("file-drop-zone.properties").getPath());
    } else if (args.length == 1) {
      configurationFile = new File(args[0]);
    } else {
      LOG.error("Too many arguments: {}", args.length);
      return;
    }

    PollingServiceManager pollingServiceManager = new PollingServiceManager(configurationFile);
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
