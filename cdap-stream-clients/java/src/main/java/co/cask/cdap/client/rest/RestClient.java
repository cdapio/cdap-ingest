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

import co.cask.common.http.exception.HttpFailureException;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Provides way to execute http requests with Apache HttpClient {@link org.apache.http.client.HttpClient}.
 */
public class RestClient {

  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

  private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";
  private static final String CONTINUUITY_API_KEY_HEADER_NAME = "X-Continuuity-ApiKey";

  private final RestClientConnectionConfig config;
  private final URI baseUrl;
  private final String basePath;
  private final CloseableHttpClient httpClient;

  public RestClient(RestClientConnectionConfig config, HttpClientConnectionManager connectionManager) {
    this.config = config;
    this.baseUrl = URI.create(String.format("%s://%s:%d", config.isSSL() ? HTTPS_PROTOCOL : HTTP_PROTOCOL,
                        config.getHost(), config.getPort()));
    this.basePath =
      "v2".equals(config.getVersion())
        ? String.format("/%s", config.getVersion())
        : String.format("/%s/namespaces/%s", config.getVersion(), config.getNamespace());

    this.httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
  }

  /**
   * Method for execute HttpRequest with authorized headers, if need.
   *
   * @param request {@link HttpRequestBase} initiated http request with entity, headers, request uri and all another
   *                required properties for successfully request
   * @return {@link CloseableHttpResponse} as a result of http request execution.
   * @throws IOException in case of a problem or the connection was aborted
   */
  public CloseableHttpResponse execute(HttpRequestBase request) throws IOException {
    if (config.isAuthEnabled()) {
      request.setHeader(HTTP_HEADER_AUTHORIZATION, config.getAuthTokenType() + " " + config.getAuthToken());
    }
    if (StringUtils.isNotEmpty(config.getAPIKey())) {
      request.setHeader(CONTINUUITY_API_KEY_HEADER_NAME, config.getAPIKey());
    }
    LOG.debug("Execute Http Request: {}", request);
    return httpClient.execute(request);
  }

  /**
   * Utility method for analysis http response status code and throw appropriate Java API Exception.
   *
   * @param response {@link HttpResponse} http response
   */
  public static void responseCodeAnalysis(HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    switch (code) {
      case HttpStatus.SC_OK:
        LOG.debug("Success operation result code");
        break;
      case HttpStatus.SC_NOT_FOUND:
        throw new HttpFailureException("Not found HTTP code was received from gateway server.", code);
      case HttpStatus.SC_CONFLICT:
        throw new HttpFailureException("Conflict HTTP code was received from gateway server.", code);
      case HttpStatus.SC_BAD_REQUEST:
        throw new HttpFailureException("Bad request HTTP code was received from gateway server.", code);
      case HttpStatus.SC_UNAUTHORIZED:
        throw new HttpFailureException(response.toString(), code);
      case HttpStatus.SC_FORBIDDEN:
        throw new HttpFailureException("Forbidden HTTP code was received from gateway server", code);
      case HttpStatus.SC_METHOD_NOT_ALLOWED:
        throw new HttpFailureException(response.getStatusLine().getReasonPhrase(), code);
      case HttpStatus.SC_NOT_ACCEPTABLE:
        throw new HttpFailureException("Input was not acceptable", code);
      case HttpStatus.SC_INTERNAL_SERVER_ERROR:
        throw new HttpFailureException("Internal server exception during operation process.", code);
      case HttpStatus.SC_NOT_IMPLEMENTED:
      default:
        throw new UnsupportedOperationException("Operation is not supported by gateway server");
    }
  }

  /**
   * Utility method for converting {@link org.apache.http.HttpEntity} HTTP entity content to JsonObject.
   *
   * @param httpEntity {@link org.apache.http.HttpEntity}
   * @return {@link JsonObject} generated from input content stream
   * @throws IOException if entity content is not available
   */
  public static JsonObject toJsonObject(HttpEntity httpEntity) throws IOException {
    String content = toString(httpEntity);
    if (StringUtils.isEmpty(content)) {
      throw new IOException("Failed to write entity content.");
    }
    return new JsonParser().parse(content).getAsJsonObject();
  }

  /**
   * Utility method for converting {@link org.apache.http.HttpEntity} HTTP entity content to String.
   *
   * @param httpEntity {@link org.apache.http.HttpEntity}
   * @return {@link String} generated from input content stream
   * @throws IOException if HTTP entity is not available
   */
  public static String toString(HttpEntity httpEntity) throws IOException {
    if (httpEntity == null || httpEntity.getContent() == null) {
      throw new IOException("Empty HttpEntity is received.");
    }
    Charset charset = Charsets.UTF_8;
    ContentType contentType = ContentType.get(httpEntity);
    if (contentType != null && contentType.getCharset() != null) {
      charset = contentType.getCharset();
    }
    Reader reader = new InputStreamReader(httpEntity.getContent(), charset);
    try {
      return CharStreams.toString(reader);
    } finally {
      reader.close();
    }
  }

  /**
   * Method for releasing unused resources.
   *
   * @throws IOException if an I/O error occurs
   */
  public void close() throws IOException {
    httpClient.close();
  }

  /**
   * @return the base URL of Rest Service API
   */
  public URI resolve(String relativePath) {
    return baseUrl.resolve(basePath + relativePath);
  }

  /**
   * @return the version of the API
   */
  public String getVersion() {
    return config.getVersion();
  }
}
