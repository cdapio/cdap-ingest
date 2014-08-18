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

package co.cask.cdap.client.rest;

import co.cask.cdap.client.StreamWriter;
import com.google.common.net.MediaType;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * @author Alina Makogon amakogon@cybervisiontech.com
 *         Date: 8/14/14
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
    return write(str, charset, null);
  }

  @Override
  public ListenableFuture<Void> write(String str, Charset charset, Map<String, String> headers) throws
    IllegalArgumentException {
    if (StringUtils.isNotEmpty(str)) {
      return write(charset != null ? str.getBytes(charset) : str.getBytes(), headers);
    } else {
      throw new IllegalArgumentException("Input string parameter is empty.");
    }
  }

  @Override
  public ListenableFuture<Void> write(ByteBuffer buffer) {
    return write(buffer, null);
  }

  @Override
  public ListenableFuture<Void> write(ByteBuffer buffer, Map<String, String> headers) throws IllegalArgumentException {
    if (buffer != null) {
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      return write(bytes, headers);
    } else {
      throw new IllegalArgumentException("ByteBuffer parameter is empty.");
    }
  }

  private ListenableFuture<Void> write(byte[] entity, Map<String, String> headers) {
    final HttpPost postRequest = new HttpPost(restClient.getBaseUrl() + String.format("streams/%s", streamName));

    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        postRequest.setHeader(streamName + "." + entry.getKey(), entry.getValue());
      }
    }

    HttpEntity content = new ByteArrayEntity(entity);
    postRequest.setEntity(content);

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
  public ListenableFuture<Void> send(File file, MediaType type) {
    return null;
  }

  public void close() throws IOException {
    if (pool != null) {
      pool.shutdown();
    }
    if (restClient != null) {
      restClient.close();
    }
  }

  public RestClient getRestClient() {
    return restClient;
  }

  public String getStreamName() {
    return streamName;
  }

  public ListeningExecutorService getPool() {
    return pool;
  }
}
