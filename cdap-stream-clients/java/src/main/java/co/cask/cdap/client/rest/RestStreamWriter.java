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

package co.cask.cdap.client.rest;

import co.cask.cdap.client.StreamWriter;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Stream writer implementation used REST Api for write Streams to processing server.
 */
public class RestStreamWriter implements StreamWriter {
  private static final Logger LOG = LoggerFactory.getLogger(RestStreamWriter.class);

  private final RestClient restClient;
  private final String streamName;
  private final ListeningExecutorService pool;

  public RestStreamWriter(RestClient restClient, int writerPoolSize, String streamName) {
    this.restClient = restClient;
    this.streamName = streamName;
    this.pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(writerPoolSize));
  }

  @Override
  public ListenableFuture<Void> write(String str, Charset charset) {
    return write(str, charset, ImmutableMap.<String, String>of());
  }

  @Override
  public ListenableFuture<Void> write(String str, Charset charset, Map<String, String> headers) throws
    IllegalArgumentException {
    Preconditions.checkArgument(str != null, "Input string parameter is null.");
    return write(new ByteArrayEntity(charset != null ? str.getBytes(charset) : str.getBytes()), headers);
  }

  @Override
  public ListenableFuture<Void> write(ByteBuffer buffer) {
    return write(buffer, ImmutableMap.<String, String>of());
  }

  @Override
  public ListenableFuture<Void> write(ByteBuffer buffer, Map<String, String> headers) throws IllegalArgumentException {
    Preconditions.checkArgument(buffer != null, "ByteBuffer parameter is null.");
    HttpEntity content;
    if (buffer.hasArray()) {
      content = new ByteArrayEntity(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    } else {
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      content = new ByteArrayEntity(bytes);
    }
    return write(content, headers);
  }

  private ListenableFuture<Void> write(HttpEntity entity, Map<String, String> headers) {
    final HttpPost postRequest = new HttpPost(restClient.getBaseURL().resolve(
      String.format("/%s/streams/%s", restClient.getVersion(), streamName)));

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      postRequest.setHeader(streamName + "." + entry.getKey(), entry.getValue());
    }

    postRequest.setEntity(entity);

    return pool.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        CloseableHttpResponse response = restClient.execute(postRequest);
        try {
          LOG.info("Write stream execute with response: " + response);
          RestClient.responseCodeAnalysis(response);
        } finally {
          response.close();
        }
        return null;
      }
    });
  }

  @Override
  public void close() throws IOException {
    pool.shutdown();
    restClient.close();
  }

  public String getStreamName() {
    return streamName;
  }
}
