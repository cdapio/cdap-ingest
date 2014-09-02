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

import com.google.common.net.MediaType;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Provides ability for ingesting events to the Stream in different ways.
 */
public interface StreamWriter extends Closeable {
  /**
   * Ingest the Stream event with a string as body.
   *
   * @param str     The string body
   * @param charset Charset to encode the string as the Stream event payload
   * @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
   * failed. Cancelling the returning future has no effect.
   */
  ListenableFuture<Void> write(String str, Charset charset);

  /**
   * Ingest a stream event with a set of headers and a string as body.
   *
   * @param str     The string body
   * @param headers Set of headers for the Stream event
   * @param charset Charset to encode the string as the Stream event payload
   * @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
   * failed. Cancelling the returning future has no effect.
   */
  ListenableFuture<Void> write(String str, Charset charset, Map<String, String> headers);

  /**
   * Ingest the Stream event with content in a {@link ByteBuffer} as body.
   *
   * @param buffer Contains the content for the Stream event body. All remaining bytes of the {@link ByteBuffer}
   *               should be used as the body. After this method returns and on the completion of the resulting
   *               {@link ListenableFuture}, the buffer content as well as properties should be unchanged.
   * @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
   * failed. Cancelling the returning future has no effect.
   */
  ListenableFuture<Void> write(ByteBuffer buffer);

  /**
   * Ingest the Stream event with a set of headers and use content in a {@link ByteBuffer} as body.
   *
   * @param buffer  Contains the content for the Stream event body. All remaining bytes of the {@link ByteBuffer}
   *                should be used as the body. After this method returns and on the completion of the resulting
   *                {@link ListenableFuture}, the buffer content as well as properties should be unchanged.
   * @param headers Set of headers for the Stream event
   * @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
   * failed. Cancelling the returning future has no effect.
   */
  ListenableFuture<Void> write(ByteBuffer buffer, Map<String, String> headers);

  /**
   * Sends the content of a {@link File} as multiple Stream events.
   *
   * @param file The file to send
   * @param type Contains information about the file type.
   * @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
   * failed. Cancelling the returning future has no effect.
   * <p/>
   * NOTE: There will be a new HTTP API in 2.5 to support extracting events from the file based on the content type.
   * Until that is available, breaking down the file content into multiple events need to happen in the client
   * side.
   */
  ListenableFuture<Void> send(File file, MediaType type);

  /**
   * Closes a {@link org.apache.http.impl.client.CloseableHttpClient} instance and a
   * {@link com.google.common.util.concurrent.ListeningExecutorService} Executor thread pool
   * for releasing all unused resource associated with them.
   *
   * @throws IOException if an I/O error occurs during close process
   */
  @Override
  void close() throws IOException;
}
