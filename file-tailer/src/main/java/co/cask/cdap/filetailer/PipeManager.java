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

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.tailer.LogTailer;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates and manage pipes.
 */
public class PipeManager extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(PipeManager.class);

  private final List<Pipe> pipeList = new ArrayList<Pipe>();

  private final File confFile;

  PipeManager(File confFile) {
    this.confFile = confFile;
  }

  /**
   * Pipes setup
   *
   * @throws IOException if can not create client stream
   */
  public void setupPipes() throws IOException {
    try {
      List<PipeConfiguration> pipeConfList = getPipeConfigList();
      for (PipeConfiguration pipeConf : pipeConfList) {
        FileTailerQueue queue = new FileTailerQueue(pipeConf.getQueueSize());
        StreamWriter writer = getStreamWriterForPipe(pipeConf);
        FileTailerStateProcessor stateProcessor =
          new FileTailerStateProcessorImpl(pipeConf.getDaemonDir(), pipeConf.getStateFile());
        FileTailerMetricsProcessor metricsProcessor =
          new FileTailerMetricsProcessor(pipeConf.getDaemonDir(), pipeConf.getStatisticsFile(),
                                         pipeConf.getStatisticsSleepInterval(), pipeConf.getPipeName(),
                                         pipeConf.getSourceConfiguration().getFileName());
        pipeList.add(new Pipe(new LogTailer(pipeConf, queue, stateProcessor, metricsProcessor),
                               new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                                  stateProcessor, metricsProcessor,
                                                  pipeConf.getSinkConfiguration().getPackSize()),
                               metricsProcessor));
      }
    } catch (ConfigurationLoadingException e) {
      throw new ConfigurationLoadingException("Error during loading configuration from file: "
                                                + confFile.getAbsolutePath() + e.getMessage());
    }
  }

  /**
   * Get pipes configuration
   *
   * @return the pipes configuration read from the configuration file
   * @throws ConfigurationLoadingException if can not load configuration
   */
  private List<PipeConfiguration> getPipeConfigList() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(confFile);
    return configuration.getPipesConfiguration();
  }

  /**
   * Create StreamWriter for pipe
   *
   * @return the pipe's streamWriter
   * @throws IOException streamWriter creation failed
   */
  private StreamWriter getStreamWriterForPipe(PipeConfiguration pipeConf) throws IOException {
    StreamClient client = pipeConf.getSinkConfiguration().getStreamClient();
    String streamName = pipeConf.getSinkConfiguration().getStreamName();
    try {
      client.create(streamName);
      return client.createWriter(streamName);
    } catch (IOException e) {
      client.close();
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    }
  }

  @Override
  public void startUp() {
    for (Pipe pipe : pipeList) {
      pipe.startUp();
    }
  }

  @Override
  public void shutDown() {
    for (Pipe pipe : pipeList) {
      try {
        pipe.shutDown();
      } catch (Exception e) {
        LOG.warn("Cannot stop pipe: {}", e);
      }
    }
  }
}
