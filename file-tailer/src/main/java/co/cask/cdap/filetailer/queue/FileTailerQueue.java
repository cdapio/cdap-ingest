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

package co.cask.cdap.filetailer.queue;

import co.cask.cdap.filetailer.event.FileTailerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * File Tailer Queue presentation
 */
public class FileTailerQueue {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerQueue.class);

  private final LinkedBlockingQueue<FileTailerEvent> queue;

  public FileTailerQueue(int size) {
    this.queue = new LinkedBlockingQueue<FileTailerEvent>(size);
  }

  /**
   * Put event into queue
   *
   * @param event the event
   * @throws InterruptedException in case interrupted while waiting
   */
  public void put(FileTailerEvent event) throws InterruptedException {
    LOG.trace("Attempt to put event {} to queue", event);
    queue.put(event);
    LOG.trace("Attempt to put event {} to queue was successful", event);
  }

  /**
   * Takes event from queue
   *
   * @return taken event
   * @throws InterruptedException in case interrupted while waiting
   */
  public FileTailerEvent take() throws InterruptedException {
    LOG.trace("Attempt to take event from queue");
    FileTailerEvent event = queue.take();
    LOG.trace("Attempt to take event {} from queue was successful", event);
    return event;
  }

  /**
   * Drains events from this queue to specific collection
   *
   * @param collection the collection to which drain events
   * @param max the maximum value to drain
   * @throws InterruptedException in case interrupted while waiting
   */
  public void drainTo(Collection<? super FileTailerEvent> collection,
                                       int max) throws InterruptedException {
    LOG.trace("Attempt to take {} events from queue", max);
    queue.drainTo(collection, max);
    LOG.trace("{} events taken from queue was successfully", collection.size());
  }
}
