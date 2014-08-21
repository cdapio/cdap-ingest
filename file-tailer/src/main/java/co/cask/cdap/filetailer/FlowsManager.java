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
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.FlowConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.tailer.LogTailer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create and manage flows
 */

public class FlowsManager {
  private final String confPath;
  private List<Flow> flowfList = new ArrayList<Flow>();

  public FlowsManager(String confPath) {
    this.confPath = confPath;
  }

  public void setupFlows() throws IOException {
    try {
      List<FlowConfiguration> flowConfList = getFlowsConfigList();
      for (FlowConfiguration flowConf : flowConfList) {
        FileTailerQueue queue = new FileTailerQueue(flowConf.getQueueSize());
        StreamWriter writer = getStreamWriterForFlow(flowConf);
        FileTailerStateProcessor stateProcessor =
          new FileTailerStateProcessorImpl(flowConf.getStateDir(), flowConf.getStateFile());
        new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE, stateProcessor);
        flowfList.add(new Flow(new LogTailer(flowConf, queue, stateProcessor),
                               new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                                  stateProcessor, flowConf.getSinkConfiguration().getPackSize())));
      }
    } catch (ConfigurationLoadingException e) {
      throw new ConfigurationLoadingException("Error during loading configuration from file: "
                                                + confPath + e.getMessage());
    }
  }

  private List<FlowConfiguration> getFlowsConfigList() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(confPath);
    return configuration.getFlowsConfiguration();
  }

  private StreamWriter getStreamWriterForFlow(FlowConfiguration flowConf) throws IOException {
    StreamClient client = flowConf.getSinkConfiguration().getStreamClient();
    String streamName = flowConf.getSinkConfiguration().getStreamName();
    try {
      client.create(streamName);
      StreamWriter writer = client.createWriter(streamName);
      return writer;
    } catch (IOException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    }
  }

  public void startFlows() {
    for (Flow flow : flowfList) {
      flow.start();
    }
  }

  public void stopFlows() {
    for (Flow flow : flowfList) {
      flow.stop();
    }
  }
}
