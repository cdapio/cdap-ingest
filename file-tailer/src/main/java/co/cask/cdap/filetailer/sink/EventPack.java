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
import co.cask.cdap.filetailer.state.FileTailerState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EventPack is design to represent pack of FileTailerEvents
 */
class EventPack {

  private final int capacity;

  private final List<FileTailerEvent> events;

  EventPack(int capacity) {
    this.capacity = capacity;
    this.events = new ArrayList<FileTailerEvent>(capacity);
  }

  boolean add(FileTailerEvent event) {
    if (events.size() < capacity) {
      return events.add(event);
    } else {
      return false;
    }
  }

  boolean isFull() {
    return capacity == events.size();
  }

  boolean isEmpty() {
    return events.isEmpty();
  }

  FileTailerState getState() {
    FileTailerState finalState = events.get(0).getState();
    for (FileTailerEvent event : events) {
      FileTailerState tmpState = event.getState();
      if (tmpState.getLastModifyTime() >= finalState.getLastModifyTime() &&
        tmpState.getPosition() > finalState.getPosition()) {
        finalState = tmpState;
      }
    }
    return finalState;
  }

  void clear() {
    events.clear();
  }

  List<FileTailerEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }
}