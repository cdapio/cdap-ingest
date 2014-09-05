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

package co.cask.cdap.filetailer.sink;

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.filetailer.BaseWorker;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Sink daemon
 */
public class FileTailerSink extends BaseWorker {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerSink.class);

  private static final int DEFAULT_PACK_SIZE = 1;
  private static final int LISTENER_THREAD_COUNT = 3;
  private static final int MAX_RETRY_COUNT = 3;
  private final FileTailerQueue queue;
  private final SinkStrategy strategy;
  private final StreamWriter writer;
  private final int packSize;
  private final Random random;

  private final FileTailerStateProcessor stateProcessor;
  private final FileTailerMetricsProcessor metricsProcessor;


  public FileTailerSink(FileTailerQueue queue, StreamWriter writer, SinkStrategy strategy,
                        FileTailerStateProcessor stateProcessor, FileTailerMetricsProcessor metricsProcessor) {
    this(queue, writer, strategy, stateProcessor, metricsProcessor, DEFAULT_PACK_SIZE);
  }

  public FileTailerSink(FileTailerQueue queue, StreamWriter writer, SinkStrategy strategy,
                        FileTailerStateProcessor stateProcessor,
                        FileTailerMetricsProcessor metricsProcessor, int packSize) {
    if (writer == null) {
      throw new IllegalArgumentException("Writer can't be empty!");
    }
    this.stateProcessor = stateProcessor;
    this.metricsProcessor = metricsProcessor;
    this.queue = queue;
    this.writer = writer;
    this.strategy = strategy;
    this.packSize = packSize;
    this.random = new Random();
  }


  @Override
  public void run() {
    LOG.debug("Creating new event pack");
    EventPack pack = new EventPack(packSize);
    while (!Thread.currentThread().isInterrupted()) {
      try {
        FileTailerEvent event = queue.take();

        pack.add(event);

        if (pack.isFull()) {
          LOG.debug("Event pack is full");
          uploadEventPack(pack);
          LOG.debug("Saving File Tailer state");
          stateProcessor.saveState(pack.getState());
          LOG.debug("Cleanup event pack");
          pack.clear();
        }
      } catch (InterruptedException e) {
        LOG.info("Sink was interrupted");
        break;
      } catch (IOException e) {
        LOG.info("Exception while sending events", e);
        break;
      }
    }
    LOG.info("Sink stopped.");
  }

  /**
   * This method blocks until all package is uploaded
   *
   * @param pack
   */
  private void uploadEventPack(EventPack pack) throws InterruptedException, IOException {
    List<FileTailerEvent> events = pack.getEvents();
    UploadLatch upload = new UploadLatch(events.size());

    for (FileTailerEvent event : events) {
      uploadEvent(upload, event);
    }

    upload.await();

    if (!upload.isSuccessful()) {
      List<FileTailerEvent> failedEvents = upload.getFailedEvents();
      LOG.debug("Failed to upload {} events ", failedEvents.size());
      throw new IOException("Failed to upload events!");
    }
  }

  private void uploadEvent(UploadLatch latch, FileTailerEvent event) throws IOException {
    uploadEvent(latch, event, 0);
  }

  private void uploadEvent(UploadLatch latch, FileTailerEvent event, int retryCount) throws IOException {
    LOG.debug("Uploading event {} with writer {}. Attempt {} out of {} ", event, writer, retryCount, MAX_RETRY_COUNT);
    long sendStartTime = System.currentTimeMillis();
    ListenableFuture<Void> resultFuture = writer.write(event.getEventData(), event.getCharset());
    Futures.addCallback(resultFuture, new WriteCallback(event, latch, MAX_RETRY_COUNT, retryCount, sendStartTime));
  }


  class WriteCallback implements FutureCallback<Void> {
    private final FileTailerEvent event;
    private final UploadLatch latch;
    private final int maxRetryCount;
    private final int retryCount;
    private final long sendStartTime;

    WriteCallback(FileTailerEvent event, UploadLatch latch, int maxRetryCount, long sendStartTime) {
      this(event, latch, maxRetryCount, 1, sendStartTime);
    }

    WriteCallback(FileTailerEvent event, UploadLatch latch, int maxRetryCount, int retryCount, long sendStartTime) {
      this.event = event;
      this.latch = latch;
      this.maxRetryCount = maxRetryCount;
      this.retryCount = retryCount;
      this.sendStartTime = sendStartTime;
    }

    @Override
    public void onSuccess(Void aVoid) {
      LOG.debug("Event {} successfully uploaded", event);
      metricsProcessor.onIngestEventMetric((int) (System.currentTimeMillis() - sendStartTime));
      latch.reportSuccess();
    }

    @Override
    public void onFailure(Throwable throwable) {
      if (maxRetryCount == retryCount) {
        LOG.debug("Failed to upload event {}", event);
        latch.reportFailure(event);
      } else {
        try {
          uploadEvent(latch, event, retryCount + 1);
        } catch (IOException e) {
          LOG.debug("Failed to upload event", e);
          latch.reportFailure(event);
        }
      }
    }
  }

}
