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

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.security.authentication.client.AbstractAuthenticationClient;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.ws.rs.core.MediaType;

/**
 * Stream client implementation used REST Api for stream management.
 */
public class RestStreamClient implements StreamClient {
  private static final Logger LOG = LoggerFactory.getLogger(RestStreamClient.class);

  private static final String DEFAULT_VERSION = "v2";
  private static final String TTL_ATTRIBUTE_NAME = "ttl";
  private static final int DEFAULT_WRITER_POOL_SIZE = 10;
  private static final Gson GSON = new Gson();

  private final RestClientConnectionConfig config;
  private final int writerPoolSize;
  private final RestClient restClient;
  private Registry<ConnectionSocketFactory> connectionRegistry;

  private RestStreamClient(Builder builder) {
    writerPoolSize = builder.writerPoolSize;
    config = new RestClientConnectionConfig(builder.host, builder.port, builder.authClient, builder.apiKey,
                                            builder.ssl, builder.version);
    if (builder.disableCertCheck) {
      try {
        connectionRegistry = AbstractAuthenticationClient.getRegistryWithDisabledCertCheck();
      } catch (KeyManagementException e) {
        LOG.error("Failed to init SSL context: {}", e);
      } catch (NoSuchAlgorithmException e) {
        LOG.error("Failed to get instance of SSL context: {}", e);
      }
    }

    restClient = new RestClient(config, createConnectionManager());
  }

  @Override
  public void create(String stream) throws IOException {
    HttpPut putRequest = new HttpPut(restClient.getBaseURL().resolve(String.format("/%s/streams/%s",
                                                                                   restClient.getVersion(), stream)));
    CloseableHttpResponse httpResponse = restClient.execute(putRequest);
    try {
      LOG.debug("Create Stream Response Code : {}", httpResponse.getStatusLine().getStatusCode());
      RestClient.responseCodeAnalysis(httpResponse);
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public void setTTL(String stream, long ttl) throws IOException {
    HttpPut putRequest = new HttpPut(restClient.getBaseURL().resolve(String.format("/%s/streams/%s/config",
                                                                                   restClient.getVersion(), stream)));
    StringEntity entity = new StringEntity(GSON.toJson(ImmutableMap.of(TTL_ATTRIBUTE_NAME, ttl)));
    entity.setContentType(MediaType.APPLICATION_JSON);
    putRequest.setEntity(entity);
    CloseableHttpResponse httpResponse = restClient.execute(putRequest);
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Set TTL Response Code : {}", responseCode);
      RestClient.responseCodeAnalysis(httpResponse);
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public long getTTL(String stream) throws IOException {
    HttpGet getRequest = new HttpGet(restClient.getBaseURL().resolve(String.format("/%s/streams/%s/info",
                                                                                   restClient.getVersion(), stream)));
    CloseableHttpResponse httpResponse = restClient.execute(getRequest);
    long ttl;
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Get TTL Response Code : {}", responseCode);
      RestClient.responseCodeAnalysis(httpResponse);
      JsonObject jsonContent = RestClient.toJsonObject(httpResponse.getEntity());
      ttl = jsonContent.get(TTL_ATTRIBUTE_NAME).getAsLong();
    } finally {
      httpResponse.close();
    }
    return ttl;
  }

  @Override
  public void truncate(String stream) throws IOException {
    HttpPost postRequest = new HttpPost(restClient.getBaseURL().resolve(
      String.format("/%s/streams/%s/truncate", restClient.getVersion(), stream)));
    CloseableHttpResponse httpResponse = restClient.execute(postRequest);
    try {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      LOG.debug("Truncate stream Response Code : {}", responseCode);
      RestClient.responseCodeAnalysis(httpResponse);
    } finally {
      httpResponse.close();
    }
  }

  @Override
  public StreamWriter createWriter(String stream) throws IOException {
    //get the Stream TTL for check does the requested Stream exist
    long ttl = getTTL(stream);
    LOG.debug("The Stream with id {} exists. Got the current Stream TTL value {} successfully.", stream, ttl);
    PoolingHttpClientConnectionManager connectionManager = createConnectionManager();
    connectionManager.setMaxTotal(writerPoolSize);
    connectionManager.setDefaultMaxPerRoute(writerPoolSize);
    RestClient writerRestClient = new RestClient(config, connectionManager);
    return new RestStreamWriter(writerRestClient, writerPoolSize, stream);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  private PoolingHttpClientConnectionManager createConnectionManager() {
    if (connectionRegistry != null) {
      return new PoolingHttpClientConnectionManager(connectionRegistry);
    } else {
      return new PoolingHttpClientConnectionManager();
    }
  }

  /**
   * Create builder for build RestStreamClient instance.
   *
   * @param host gateway server host
   * @param port gateway server port
   * @return {@link Builder} Builder instance
   */
  public static Builder builder(String host, int port) {
    return new Builder(host, port);
  }

  /**
   * Class Builder for create RestStreamClient instance.
   */
  public static class Builder {
    //mandatory
    private final int port;
    private final String host;

    //optional
    private AuthenticationClient authClient;
    private String apiKey;
    private boolean ssl = false;
    private boolean disableCertCheck = false;
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

    public Builder disableCertCheck(boolean disableCertCheck) {
      this.disableCertCheck = disableCertCheck;
      return this;
    }

    public Builder authClient(AuthenticationClient authClient) {
      this.authClient = authClient;
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
}
