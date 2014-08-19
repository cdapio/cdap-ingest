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
import co.cask.cdap.filetailer.config.exception.ConfigurationLoaderException;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerState;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class
 */
public class FileTailerApplication {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerApplication.class);

  public static void main(String[] args) {
    LOG.info("Application started");

    String configurationPath =
        FileTailerApplication.class.getClassLoader().getResource("config.properties").getFile();

    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    try {
      loader.load(configurationPath);
    } catch (ConfigurationLoaderException e) {
      LOG.error("Can not load configurations form file {}: {}", configurationPath, e.getMessage());
      return;
    }

    FileTailerStateProcessor stateProcessor;
    try {
      stateProcessor = new FileTailerStateProcessorImpl(loader.getStateDir(), loader.getStateFile());
    } catch (ConfigurationLoaderException e) {
      LOG.error("Can not get property: {}", e.getMessage());
      return;
    }

    FileTailerQueue queue = new FileTailerQueue(100);

    List<StreamClient> clients;
    try {
      clients = loader.getStreamClients();
    } catch (ConfigurationLoaderException e) {
      LOG.error("Can not get Stream Clients list: {}", e.getMessage());
      return;
    }

    String streamName;
    try {
      streamName = loader.getStreamName();
    } catch (ConfigurationLoaderException e) {
      LOG.error("Can not get stream name: {}", e.getMessage());
      return;
    }

    List<StreamWriter> writers = new ArrayList<StreamWriter>(clients.size());
    for (StreamClient client : clients) {
      try {
        client.create(streamName);
        writers.add(client.createWriter(streamName));
      } catch (IOException e) {
        LOG.error("Can not create/get client stream by name {}: {}", streamName, e.getMessage());
        return;
      }
    }

    FileTailerSink sink =
        new FileTailerSink(queue, writers, SinkStrategy.LOADBALANCE, stateProcessor);

    sink.start();

    for (int i = 0; i < 10; i++) {
      FileTailerState state = new FileTailerState("test.log", i, (i), System.currentTimeMillis());
      try {
        queue.put(new FileTailerEvent(state, String.valueOf(i), Charset.defaultCharset()));
      } catch (InterruptedException e) {
        LOG.warn("Failed to submit event");
      }
    }

  }

}
