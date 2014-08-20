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

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.tailer.LogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class
 */
public class FileTailerApplication {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerApplication.class);

  public static void main(String[] args) {
    LOG.info("Application started");

//    String configurationPath;
//    if (args.length == 0) {
//      configurationPath = FileTailerApplication.class.getClassLoader().getResource("config.properties").getFile();
//    } else if (args.length == 1) {
//      configurationPath = args[0];
//    } else {
//      LOG.error("Too many arguments: {}", args.length);
//      return;
//    }
//
//    ConfigurationLoader loader = new ConfigurationLoaderImpl();
//    try {
//      loader.load(configurationPath);
//    } catch (ConfigurationLoadingException e) {
//      LOG.error("Can not load configurations form file {}: {}", configurationPath, e.getMessage());
//      return;
//    }
//
//    FileTailerQueue queue = new FileTailerQueue(loader.getQueueSize());
//    FileTailerStateProcessor
//            stateProcessor = new FileTailerStateProcessorImpl(loader.getStateDir(), loader.getStateFile());
//    List<StreamClient> clients = loader.getStreamClients();
//    String streamName = loader.getStreamName();
//    List<StreamWriter> writers = new ArrayList<StreamWriter>(clients.size());
//    for (StreamClient client : clients) {
//        try {
//            client.create(streamName);
//            writers.add(client.createWriter(streamName));
//        } catch (IOException e) {
//            LOG.error("Can not create/get client stream by name {}: {}", streamName, e.getMessage());
//            return;
//        }
//    }
//
//    FileTailerSink sink = new FileTailerSink(queue, writers, SinkStrategy.LOADBALANCE, stateProcessor);
//    LogTailer tailer = new LogTailer(loader, queue, stateProcessor);
//
//    sink.start();
//    tailer.start();
  }

}
