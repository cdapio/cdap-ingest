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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
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

/**
 * The utility class for working with rest clients.
 */
public final class RestClientUtils {
  private static final Logger LOG = LoggerFactory.getLogger(RestClientUtils.class);

  private RestClientUtils() {
  }

  /**
   * Utility method for converting {@link org.apache.http.HttpEntity} HTTP entity content to JsonObject.
   *
   * @param httpEntity {@link org.apache.http.HttpEntity}
   * @return {@link com.google.gson.JsonObject} generated from input content stream
   * @throws IOException if entity content is not available
   */
  public static JsonObject toJsonObject(HttpEntity httpEntity) throws IOException {
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
      String content = CharStreams.toString(reader);
      if (StringUtils.isEmpty(content)) {
        throw new IOException("Failed to write entity content.");
      }
      return new JsonParser().parse(content).getAsJsonObject();
    } finally {
      reader.close();
    }
  }

  /**
   * Utility method for analysis http response status code and throw appropriate Java API Exception
   *
   * @param response {@link org.apache.http.HttpResponse} http response
   */
  public static void verifyResponseCode(HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    switch (code) {
      case HttpStatus.SC_OK:
        LOG.debug("Success operation result code.");
        break;
      case HttpStatus.SC_NOT_FOUND:
        throw new NotFoundException("Not found HTTP code was received from gateway server.");
      case HttpStatus.SC_CONFLICT:
        throw new BadRequestException("Conflict HTTP code was received from gateway server.");
      case HttpStatus.SC_BAD_REQUEST:
        throw new BadRequestException("Bad request HTTP code was received from gateway server.");
      case HttpStatus.SC_UNAUTHORIZED:
        throw new NotAuthorizedException(response);
      case HttpStatus.SC_FORBIDDEN:
        throw new ForbiddenException("Forbidden HTTP code was received from gateway server");
      case HttpStatus.SC_METHOD_NOT_ALLOWED:
        throw new NotAllowedException(response.getStatusLine().getReasonPhrase());
      case HttpStatus.SC_INTERNAL_SERVER_ERROR:
        throw new InternalServerErrorException("Internal server exception during operation process.");
      case HttpStatus.SC_NOT_IMPLEMENTED:
      default:
        throw new NotSupportedException("Operation is not supported by gateway server");
    }
  }
}
