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

import co.cask.cdap.client.exception.NotFoundException;

import java.io.Closeable;
import java.io.IOException;

/**
 * The client interface to interact with services provided by stream endpoint.
 */
public interface StreamClient extends Closeable {
  /**
   * Creates a stream with the given name.
   */
  void create(String stream) throws IOException;

  /**
   * Set the Time-To-Live (TTL) property of the given stream.
   *
   * @param stream Name of the stream
   * @param ttl    TTL in seconds
   * @throws co.cask.cdap.client.exception.NotFoundException If the stream does not exists
   */
  void setTTL(String stream, long ttl) throws NotFoundException, IOException;

  /**
   * Retrieves the Time-To-Live (TTL) property of the given stream.
   *
   * @param stream Name of the stream
   * @return Current TTL of the stream in seconds
   * @throws NotFoundException If the stream does not exists
   */
  long getTTL(String stream) throws NotFoundException, IOException;

  /**
   * Truncates all existing events in the give stream.
   *
   * @param stream Name of the stream
   * @throws NotFoundException If the stream does not exists
   */
  void truncate(String stream) throws NotFoundException, IOException;

  /**
   * Creates a {@link StreamWriter} instance for writing events to the given stream.
   *
   * @param stream Name of the stream
   * @return An instance of {@link StreamWriter} that is ready for writing events to the stream
   */
  StreamWriter createWriter(String stream);

  /**
   * Closes a {@link org.apache.http.impl.client.CloseableHttpClient} instance for releasing all unused resource
   * associated with it.
   *
   * @throws IOException if an I/O error occurs during close process
   */
  void close() throws IOException;
}
