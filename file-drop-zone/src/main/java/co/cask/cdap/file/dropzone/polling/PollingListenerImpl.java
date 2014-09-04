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

package co.cask.cdap.file.dropzone.polling;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.file.dropzone.config.ObserverConfiguration;
import co.cask.cdap.filetailer.Pipe;
import co.cask.cdap.filetailer.PipeListener;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.sink.SinkStrategy;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import co.cask.cdap.filetailer.state.FileTailerStateProcessorImpl;
import co.cask.cdap.filetailer.tailer.LogTailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The listener for polling dirs with some time interval
 */
public class PollingListenerImpl implements PollingListener {

  private static final Logger LOG = LoggerFactory.getLogger(PollingListenerImpl.class);
  private PollingService monitor;
  private final ObserverConfiguration observerConf;
  private FileTailerMetricsProcessor metricsProcessor;

  public PollingListenerImpl(PollingService monitor, ObserverConfiguration observerConf) {
    this.monitor = monitor;
    this.observerConf = observerConf;
    metricsProcessor = new FileTailerMetricsProcessor(observerConf.getDaemonDir(),
                                                      observerConf.getPipeConf().getStatisticsFile(),
                                                      observerConf.getPipeConf().getStatisticsSleepInterval(),
                                                      observerConf.getPipeConf().getPipeName(),
                                                      observerConf.getPipeConf().
                                                        getSourceConfiguration().getWorkDir());
    metricsProcessor.startWorker();
  }

  @Override
  public void onFileCreate(File file) {
    LOG.info("File Added: {}", file.getAbsolutePath());
    Pipe pipe;
    try {
      LOG.debug("Start configure pipe for file: {}", file.getAbsolutePath());
      pipe = setupPipe(file);
      LOG.debug("Pipe for file {} successfully configured", file.getAbsolutePath());
    } catch (IOException e) {
      LOG.error("Error during pipe setup: {}", e.getMessage());
      return;
    }
    LOG.info("Start processing file: {}", file.getAbsolutePath());
    pipe.startWithoutMetrics();
  }

  @Override
  public void onException(Exception exception) {
    LOG.warn("Error", exception);
    metricsProcessor.stopWorker();
    monitor.stopDirMonitor(new File(observerConf.getPipeConf().getSourceConfiguration().getWorkDir()));
  }

  /**
   * Pipe setup
   *
   * @throws IOException if can not create client stream
   */
  private Pipe setupPipe(File file) throws IOException {
    PipeConfiguration pipeConfiguration = observerConf.getPipeConf().getPipeConfiguration(file.getName());
    FileTailerQueue queue = new FileTailerQueue(pipeConfiguration.getQueueSize());
    StreamWriter writer = getStreamWriterForPipe(pipeConfiguration);
    FileTailerStateProcessor stateProcessor =
      new FileTailerStateProcessorImpl(observerConf.getDaemonDir(), pipeConfiguration.getStateFile());
    PipeListener pipeListener = new PipeListenerImpl(pipeConfiguration.getSourceConfiguration().getWorkDir(),
                                                     file.getAbsolutePath(), observerConf.getDaemonDir() +
      "/" + pipeConfiguration.getStateFile());
    Pipe pipe = new Pipe(new LogTailer(pipeConfiguration, queue, stateProcessor, metricsProcessor, pipeListener),
                         new FileTailerSink(queue, writer, SinkStrategy.LOADBALANCE,
                                            stateProcessor, metricsProcessor, pipeListener,
                                            pipeConfiguration.getSinkConfiguration().getPackSize()),
                         metricsProcessor);
    pipeListener.setPipe(pipe);
    return pipe;

  }

  /**
   * create StreamWriter for pipe
   *
   * @param pipeConf the pipe configuration
   * @return streamWriter
   * @throws java.io.IOException streamWriter creation failed
   */
  private StreamWriter getStreamWriterForPipe(PipeConfiguration pipeConf) throws IOException {
    StreamClient client = pipeConf.getSinkConfiguration().getStreamClient();
    String streamName = pipeConf.getSinkConfiguration().getStreamName();
    try {
      client.create(streamName);
      StreamWriter writer = null;
      writer = client.createWriter(streamName);
      return writer;
    } catch (IOException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    }
  }

  /**
   * Delete state file with specified path
   *
   * @param stateFilePath path to state file
   */
  private void removeStateFile(String stateFilePath) {
    File stateFile = new File(stateFilePath);
    if (stateFile.delete()) {
      LOG.info("State file successfully deleted {}.", stateFile);
    } else {
      throw new IllegalArgumentException(
        String.format("Cannot remove specified file %s.", stateFile.getAbsolutePath()));
    }
  }

  /**
   * The listener for pipes
   */
  private class PipeListenerImpl implements PipeListener {

    private boolean isRead = false;
    private String directoryPath;
    private String filePath;
    private String stateFilePath;
    private Pipe pipe;

    public PipeListenerImpl(String directoryPath, String filePath, String stateFilePath) {
      this.directoryPath = directoryPath;
      this.filePath = filePath;
      this.stateFilePath = stateFilePath;
    }

    @Override
    public void onRead() {
      LOG.info("File {} already read", filePath);
      isRead = true;
    }

    @Override
    public boolean isRead() {
      return isRead;
    }

    @Override
    public void onIngest() {
      LOG.info("File {} already processed", filePath);
      pipe.stopWithoutMetrics();
      removeStateFile(stateFilePath);
      monitor.removeFile(new File(directoryPath), new File(filePath));
    }

    @Override
    public void setPipe(Pipe pipe) {
      this.pipe = pipe;
    }
  }

}
