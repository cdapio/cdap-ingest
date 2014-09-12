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

package co.cask.cdap.filetailer.event;

import co.cask.cdap.filetailer.state.FileTailerState;

import java.nio.charset.Charset;

/**
 * FileTailerEvent is design to represent File Tailer event or log
 */
public class FileTailerEvent {

  private final FileTailerState state;
  private final String eventData;
  private final Charset charset;

  public FileTailerEvent(FileTailerState state, String eventData, Charset charset) {
    this.state = state;
    this.eventData = eventData;
    this.charset = charset;
  }

  /**
   * Retrieves the FileTailer state of this event
   *
   * @return the FileTailer state
   */
  public FileTailerState getState() {
    return state;
  }

  /**
   * Retrieves the FileTailer event data of this event
   *
   * @return the FileTailer event data
   */
  public String getEventData() {
    return eventData;
  }

  /**
   * Retrieves the charset of this event
   *
   * @return the charset
   */
  public Charset getCharset() {
    return charset;
  }

  @Override
  public String toString() {
    return new StringBuilder("FileTailerEvent{")
      .append("state=").append(state)
      .append(", eventData='").append(eventData).append('\'')
      .append('}').toString();
  }
}
