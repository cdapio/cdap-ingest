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

package co.cask.cdap.filetailer.config;

import co.cask.cdap.client.StreamClient;

/**
 * Retrieves sink properties of a pipe.
 */
public interface SinkConfiguration {

  /**
   * Retrieves the name of target stream
   *
   * @return the target stream name
   */
  String getStreamName();

  /**
   * Retrieves the Stream client
   *
   * @return the Stream client
   */
  StreamClient getStreamClient();

  /**
   * Retrieves the size of events pack, which sends to stream
   *
   * @return the size of events pack
   */
  int getPackSize();

  /**
   * Retrieves the failure retry limit (limit for the number of attempts to send event/events, if error occurred)
   *
   * @return the failure retry limit
   */
  int getFailureRetryLimit();

  /**
   * Retrieves the interval to wait, after occurred error while sending event/events
   *
   * @return the failure sleep interval
   */
  long getFailureSleepInterval();
}
