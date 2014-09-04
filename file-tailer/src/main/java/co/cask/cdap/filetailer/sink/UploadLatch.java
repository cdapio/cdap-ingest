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

import co.cask.cdap.filetailer.event.FileTailerEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by dev on 18.08.14.
 */
class UploadLatch {
  private final CountDownLatch latch;
  private final List<FileTailerEvent> failedEvents;

  UploadLatch(int size) {
    this.latch = new CountDownLatch(size);
    this.failedEvents = new ArrayList<FileTailerEvent>();
  }

  void await() throws InterruptedException {
    latch.await();
  }

  void reportSuccess() {
    latch.countDown();
  }

  void reportFailure(FileTailerEvent event) {
    synchronized (failedEvents) {
      failedEvents.add(event);
    }
    latch.countDown();
  }

  boolean isSuccessful() {
    return isComplete() && failedEvents.size() == 0;
  }

  boolean isComplete() {
    return latch.getCount() == 0;
  }

  List<FileTailerEvent> getFailedEvents() {
    return Collections.unmodifiableList(failedEvents);
  }
}
