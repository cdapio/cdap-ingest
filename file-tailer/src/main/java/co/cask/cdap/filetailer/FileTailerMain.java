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

package co.cask.cdap.filetailer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Main class
 */
public class FileTailerMain {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerMain.class);

  public static void main(String[] args) {
    LOG.info("Application started");

    File configurationFile;
    if (args.length == 0) {
      configurationFile =
        new File(FileTailerMain.class.getClassLoader().getResource("file-tailer.properties").getPath());
    } else if (args.length == 1) {
      configurationFile = new File(args[0]);
    } else {
      LOG.info("Too many arguments: {}. Requirement: 0 or 1", args.length);
      return;
    }
    PipeManager manager = new PipeManager();
    try {
      manager.setupPipes(configurationFile);
    } catch (IOException e) {
      LOG.error("Error during flows: {} setup", e.getMessage());
      return;
    }
    LOG.info("Staring flows");
    manager.startPipes();
    Runtime.getRuntime().addShutdownHook(new Thread(new PipeShutdownTask(manager)));

  }
}
