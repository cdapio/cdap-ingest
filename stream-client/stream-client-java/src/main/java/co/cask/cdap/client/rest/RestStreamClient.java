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

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.exception.NotFoundException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.ws.rs.core.MediaType;

/**
 * Stream client implementation used REST Api for stream management
 */
public class RestStreamClient implements StreamClient {
  private static final Logger LOG = LoggerFactory.getLogger(RestStreamClient.class);

  public static final String DEFAULT_VERSION = "v2";
  public static final String TTL_ATTRIBUTE_NAME = "ttl";
  public static final int DEFAULT_WRITER_POOL_SIZE = 10;

  private static final Gson GSON = new Gson();

  private final RestClientConnectionConfig config;
  private final int writerPoolSize;
  private final RestClient restClient;

  @Override
  public void create(String stream) throws IOException {
    HttpPut putRequest = new HttpPut(restClient.getBaseUrl() + String.format("streams/%s", stream));
    CloseableHttpResponse httpResponse = restClient.execute(putRequest);
    try {
      LOG.debug("Create Stream Response Code : {}", httpResponse.getStatusLine().getStatusCode());
      RestClient.responseCodeAnalysis(httpResponse);
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public void setTTL(String stream, long ttl) throws NotFoundException, IOException {
    HttpPut putRequest = new HttpPut(restClient.getBaseUrl() + String.format("streams/%s/config", stream));
    StringEntity entity = new StringEntity(GSON.toJson(ImmutableMap.of(TTL_ATTRIBUTE_NAME, ttl)));
    entity.setContentType(MediaType.APPLICATION_JSON);
    putRequest.setEntity(entity);
    CloseableHttpResponse httpResponse = restClient.execute(putRequest);
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Set TTL Response Code : {}", responseCode);
      if (responseCode == HttpStatus.SC_NOT_FOUND) {
        throw new NotFoundException("Stream", stream);
      } else {
        RestClient.responseCodeAnalysis(httpResponse);
      }
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public long getTTL(String stream) throws NotFoundException, IOException {
    HttpGet getRequest = new HttpGet(restClient.getBaseUrl() + String.format("streams/%s/info", stream));
    CloseableHttpResponse httpResponse = restClient.execute(getRequest);
    long ttl;
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Get TTL Response Code : {}", responseCode);
      if (responseCode == HttpStatus.SC_NOT_FOUND) {
        throw new NotFoundException("Stream", stream);
      } else {
        RestClient.responseCodeAnalysis(httpResponse);
      }
      JsonObject jsonContent = RestClient.getEntityAsJsonObject(httpResponse.getEntity());
      ttl = jsonContent.get(TTL_ATTRIBUTE_NAME).getAsLong();
    } finally {
      httpResponse.close();
    }
    return ttl;
  }

  @Override
  public void truncate(String stream) throws NotFoundException, IOException {
    HttpPost postRequest = new HttpPost(restClient.getBaseUrl() + String.format("streams/%s/truncate", stream));
    CloseableHttpResponse httpResponse = restClient.execute(postRequest);
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Truncate stream Response Code : {}", responseCode);
      if (responseCode == HttpStatus.SC_NOT_FOUND) {
        throw new NotFoundException("Stream", stream);
      } else {
        RestClient.responseCodeAnalysis(httpResponse);
      }
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public StreamWriter createWriter(String stream) throws NotFoundException, IOException {
    //get the Stream TTL for check does the requested Stream exist
    long ttl = getTTL(stream);
    LOG.debug("The Stream with id {} exists. Got the current Stream TTL value {} successfully.", stream, ttl);
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(writerPoolSize);
    connectionManager.setDefaultMaxPerRoute(writerPoolSize);
    RestClient writerRestClient = new RestClient(config, connectionManager);
    return new RestStreamWriter(writerRestClient, writerPoolSize, stream);
  }

  public void close() throws IOException {
    restClient.close();
  }

  /**
   * Class Builder for create RestStreamClient instance
   */
  public static class Builder {
    //mandatory
    private final int port;
    private final String host;

    //optional
    private String authToken;
    private String apiKey;
    private boolean ssl = false;
    private int writerPoolSize = DEFAULT_WRITER_POOL_SIZE;
    private String version = DEFAULT_VERSION;

    public Builder(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public Builder ssl(boolean ssl) {
      this.ssl = ssl;
      return this;
    }

    public Builder authToken(String authToken) {
      this.authToken = authToken;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder writerPoolSize(int writerPoolSize) {
      this.writerPoolSize = writerPoolSize;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public RestStreamClient build() {
      return new RestStreamClient(this);
    }
  }

  private RestStreamClient(Builder builder) {
    writerPoolSize = builder.writerPoolSize;
    config = new RestClientConnectionConfig(builder.host, builder.port, builder.authToken, builder.apiKey,
                                            builder.ssl, builder.version);
    restClient = new RestClient(config, new PoolingHttpClientConnectionManager());
  }

  public RestClient getRestClient() {
    return restClient;
  }

  public int getWriterPoolSize() {
    return writerPoolSize;
  }

}
