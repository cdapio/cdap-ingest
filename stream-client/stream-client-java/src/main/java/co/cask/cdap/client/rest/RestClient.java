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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
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
import java.nio.charset.Charset;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.HttpHeaders;

/**
 * Provides way to execute http requests with Apache HttpClient {@link org.apache.http.client.HttpClient}
 */
public class RestClient {

  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";
  private static final String CONTINUUITY_API_KEY_HEADER_NAME = "X-Continuuity-ApiKey";
  private static final String AUTHENTICATION_HEADER_PREFIX_BEARER = "Bearer ";
  private static final String FORWARD_SLASH = "/";
  private static final String PROTOCOL_POSTFIX = "://";
  private static final String COLON = ":";

  private final RestClientConnectionConfig config;
  private final String baseUrl;
  private final CloseableHttpClient httpClient;

  public RestClient(RestClientConnectionConfig config, HttpClientConnectionManager connectionManager) {
    this.config = config;
    this.baseUrl = config.isSsl() ? HTTPS_PROTOCOL : HTTP_PROTOCOL + PROTOCOL_POSTFIX + config.getHost() +
      COLON + config.getPort() + FORWARD_SLASH + config.getVersion() + FORWARD_SLASH;
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
    if (StringUtils.isNotEmpty(config.getAuthToken())) {
      request.setHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_HEADER_PREFIX_BEARER + config.getAuthToken());
    }
    if (StringUtils.isNotEmpty(config.getApiKey())) {
      request.setHeader(CONTINUUITY_API_KEY_HEADER_NAME, config.getApiKey());
    }
    LOG.debug("Execute Http Request: " + request);
    return httpClient.execute(request);
  }

  /**
   * Utility method for analysis http response status code and throw appropriate Java API Exception
   *
   * @param response {@link HttpResponse} http response
   */
  public static void responseCodeAnalysis(HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    switch (code) {
      case HttpStatus.SC_OK:
        LOG.info("Success operation result code");
        break;
      case HttpStatus.SC_NOT_FOUND:
        throw new NotFoundException();
      case HttpStatus.SC_CONFLICT:
      case HttpStatus.SC_BAD_REQUEST:
        throw new BadRequestException();
      case HttpStatus.SC_UNAUTHORIZED:
        throw new NotAuthorizedException(response);
      case HttpStatus.SC_FORBIDDEN:
        throw new ForbiddenException();
      case HttpStatus.SC_METHOD_NOT_ALLOWED:
        throw new NotAllowedException(response.getStatusLine().getReasonPhrase());
      case HttpStatus.SC_INTERNAL_SERVER_ERROR:
        throw new InternalServerErrorException("Internal server exception during operation process.");
      case HttpStatus.SC_NOT_IMPLEMENTED:
      default:
        throw new NotSupportedException("Operation is not supported");
    }
  }

  /**
   * Utility method for convert {@link org.apache.http.HttpEntity} http entity content to JsonObject
   *
   * @param httpEntity {@link org.apache.http.HttpEntity}
   * @return {@link JsonObject} generated from input content stream
   * @throws IOException in case if entity content is not available
   */
  public static JsonObject getEntityAsJsonObject(HttpEntity httpEntity) throws IOException {
    JsonObject result;
    String content = getEntityAsString(httpEntity);
    if (StringUtils.isNotEmpty(content)) {
      JsonElement root = new JsonParser().parse(content);
      result = root.getAsJsonObject();
    } else {
      throw new IOException("Failed to write entity content.");
    }
    return result;
  }

  /**
   * Utility method for convert {@link org.apache.http.HttpEntity} http entity content to String
   *
   * @param httpEntity {@link org.apache.http.HttpEntity}
   * @return {@link String} generated from input content stream
   * @throws IOException in case if entity content is not available
   */
  public static String getEntityAsString(HttpEntity httpEntity) throws IOException {
    String content = null;
    if (httpEntity != null && httpEntity.getContent() != null) {
      Charset charset;
      ContentType contentType = ContentType.getOrDefault(httpEntity);
      if (contentType != null && contentType.getCharset() != null) {
        charset = contentType.getCharset();
      } else {
        charset = Charsets.UTF_8;
      }
      Reader reader = new InputStreamReader(httpEntity.getContent(), charset);
      try {
        content = CharStreams.toString(reader);
      } finally {
        reader.close();
      }
    } else {
      throw new IOException("Empty HttpEntity is received.");
    }
    return content;
  }

  /**
   * Method for releasing unused resources
   *
   * @throws IOException if an I/O error occurs
   */
  public void close() throws IOException {
    if (httpClient != null) {
      httpClient.close();
    }
  }

  /**
   * @return the base URL of Rest Service API
   */
  public String getBaseUrl() {
    return baseUrl;
  }
}
