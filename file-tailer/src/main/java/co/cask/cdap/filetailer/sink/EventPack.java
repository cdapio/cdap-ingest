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

package co.cask.cdap.filetailer.sink;

import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.state.FileTailerState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a "pack" of FileTailerEvents.
 */
class EventPack {

  private final int capacity;

  private final List<FileTailerEvent> events;

  EventPack(int capacity) {
    this.capacity = capacity;
    this.events = new ArrayList<FileTailerEvent>(capacity);
  }

  /**
   * Adds all events from a specified list to this pack.
   *
   * @param events the list of events
   * @return the result of adding the list
   */
  boolean addAll(List<FileTailerEvent> events) {
    return this.events.size() + events.size() <= capacity && this.events.addAll(events);
  }

  /**
   * Retrieves is this pack is full [true|false]
   *
   * @return <code>true</code> if this pack is full; <code>false</code> otherwise
   */
  boolean isFull() {
    return capacity == events.size();
  }

  /**
   * Retrieves the free size of this pack.
   *
   * @return the free size of this pack
   */
  int getFreeSize() {
    return capacity - events.size();
  }

  /**
   * Retrieves the state of this pack.
   * State of pack it is a state of some event from pack with the highest values:
   * last time modified and position
   *
   * @return state of this pack or null in case pack is empty
   */
  FileTailerState getState() {
    if (events.isEmpty()) {
      return null;
    }
    FileTailerState finalState = events.get(0).getState();
    for (FileTailerEvent event : events) {
      FileTailerState tmpState = event.getState();
      if (tmpState.getLastModifyTime() > finalState.getLastModifyTime() ||
        tmpState.getLastModifyTime() == finalState.getLastModifyTime() &&
          tmpState.getPosition() > finalState.getPosition()) {
        finalState = tmpState;
      }
    }
    return finalState;
  }

  /**
   * Clears the event pack.
   */
  void clear() {
    events.clear();
  }

  /**
   * Retrieves all events from this pack.
   *
   * @return the events from this pack
   */
  List<FileTailerEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }
}
