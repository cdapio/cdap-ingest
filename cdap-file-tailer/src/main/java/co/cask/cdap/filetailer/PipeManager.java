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
import com.google.common.util.concurrent.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Creates and manage pipes.
 */
public class PipeManager extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(PipeManager.class);

  private final List<Pipe> pipeList = new ArrayList<Pipe>();
  private final File confFile;
  private final ServiceManager serviceManager;

  public PipeManager(File confFile) {
    this.confFile = confFile;
    serviceManager = createManager();
  }

  /**
   * Pipes setup
   *
   * @throws IOException in case a client stream cannot be created
   */
  private ServiceManager setupPipes() throws IOException {
    StreamClient client = null;
    StreamWriter writer = null;
    try {
      List<PipeConfiguration> pipeConfList = getPipeConfigs();
      for (PipeConfiguration pipeConf : pipeConfList) {
        FileTailerQueue queue = new FileTailerQueue(pipeConf.getQueueSize());
        client = pipeConf.getSinkConfiguration().getStreamClient();
        String streamName = pipeConf.getSinkConfiguration().getStreamName();
        writer = getStreamWriterForPipe(client, streamName);
        FileTailerStateProcessor stateProcessor =
          new FileTailerStateProcessorImpl(pipeConf.getDaemonDir(), pipeConf.getStateFile());
        FileTailerMetricsProcessor metricsProcessor =
          new FileTailerMetricsProcessor(pipeConf.getDaemonDir(), pipeConf.getStatisticsFile(),
                                         pipeConf.getStatisticsSleepInterval(), pipeConf.getPipeName(),
                                         pipeConf.getSourceConfiguration().getFileName());
        pipeList.add(new Pipe(new LogTailer(pipeConf, queue, stateProcessor, metricsProcessor, null),
                               new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                                  stateProcessor, metricsProcessor, null,
                                                  pipeConf.getSinkConfiguration().getPackSize()),
                               metricsProcessor));
        client = null;
        writer = null;
      }
      return new ServiceManager(pipeList);
    } catch (ConfigurationLoadingException e) {
      throw new ConfigurationLoadingException("Error during loading configuration from file: "
                                                + confFile.getAbsolutePath() + e.getMessage());
    } finally {
      if (client != null) {
        client.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * Retrieves {@link ServiceManager} for all pipes of this {@link PipeManager}
   *
   * @return the {@link ServiceManager}
   */
  private ServiceManager createManager() {
    try {
      return setupPipes();
    } catch (IOException e) {
      LOG.error("Error during pipes: {} setup", e);
      throw new RuntimeException("Error during pipes setup. Cannot start daemon.");
    }
  }

  /**
   * Get pipes configuration
   *
   * @return the pipes configuration read from the configuration file
   * @throws ConfigurationLoadingException in case can not load configuration
   */
  private List<PipeConfiguration> getPipeConfigs() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(confFile);
    return configuration.getPipeConfigurations();
  }

  /**
   * Create StreamWriter for pipe
   *
   * @return the pipe's streamWriter
   * @throws IOException streamWriter creation failed
   */
  private StreamWriter getStreamWriterForPipe(StreamClient client,  String streamName) throws IOException {
    try {
      client.create(streamName);
      return client.createWriter(streamName);
    } catch (IOException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    }
  }

  @Override
  public void startUp() {
    serviceManager.startAsync().awaitHealthy();
  }

  @Override
  public void shutDown() {
    try {
      serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      LOG.warn("Cannot stop pipes: {}", e);
    }
  }
}
