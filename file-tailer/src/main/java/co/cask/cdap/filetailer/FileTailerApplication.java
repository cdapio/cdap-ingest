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

package co.cask.cdap.filetailer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class
 */
public class FileTailerApplication {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerApplication.class);

  public static void main(String[] args) {
    LOG.info("Application started");

    String configurationPath;
    if (args.length == 0) {
      configurationPath = FileTailerApplication.class.getClassLoader().getResource("config.properties").getFile();
    } else if (args.length == 1) {
      configurationPath = args[0];
    } else {
      LOG.error("Too many arguments: {}", args.length);
      return;
    }
      PipeManager manager = new PipeManager(configurationPath);
      try {
          manager.setupFlows();
      } catch (IOException e) {
          LOG.error("Error during flows: {} setup", e.getMessage());
          return;
      }
     LOG.info("Staring flows");
     manager.startFlows();
     Runtime.getRuntime().addShutdownHook(new Thread(new PipeShutdownTask(manager)));
  }

}
