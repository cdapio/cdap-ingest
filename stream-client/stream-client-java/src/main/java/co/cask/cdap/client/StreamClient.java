/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The client interface to interact with services provided by the Stream endpoint.
 */
public interface StreamClient extends Closeable {
  /**
   * Creates the Stream with the given name.
   *
   * @param stream String value of the Stream id
   * @throws IOException in case of a problem or the connection was aborted
   */
  void create(String stream) throws IOException;

  /**
   * Set the Time-To-Live (TTL) property of the given Stream.
   *
   * @param stream Name of the Stream
   * @param ttl    TTL in seconds
   * @throws IOException in case of a problem or the connection was aborted
   */
  void setTTL(String stream, long ttl) throws IOException;

  /**
   * Retrieves the Time-To-Live (TTL) property of the given Stream.
   *
   * @param stream Name of the Stream
   * @return Current TTL of the Stream in seconds
   * @throws IOException in case of a problem or the connection was aborted
   */
  long getTTL(String stream) throws IOException;

  /**
   * Truncates all existing events in the give Stream.
   *
   * @param stream Name of the Stream
   * @throws IOException in case of a problem or the connection was aborted
   */
  void truncate(String stream) throws IOException;

  /**
   * Creates a {@link StreamWriter} instance for writing events to the given Stream.
   *
   * @param stream Name of the Stream
   * @return An instance of {@link StreamWriter} that is ready for writing events to the Stream
   * @throws IOException in case of a problem or the connection was aborted
   */
  StreamWriter createWriter(String stream) throws IOException, URISyntaxException;

  /**
   * Closes a {@link org.apache.http.impl.client.CloseableHttpClient} instance for releasing all unused resource
   * associated with it.
   *
   * @throws IOException if an I/O error occurs during close process
   */
  @Override
  void close() throws IOException;
}
