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

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;

/**
 * Contains common utility methods for unit tests for the REST Stream Client API implementation.
 */
public final class TestUtils {
  public static final String SUCCESS_STREAM_NAME = "success";
  public static final String NOT_FOUND_STREAM_NAME = "notFound";
  public static final String BAD_REQUEST_STREAM_NAME = "badRequest";
  public static final String AUTH_STREAM_NAME = "auth";
  public static final String FORBIDDEN_STREAM_NAME = "forbidden";
  public static final String NOT_ALLOWED_STREAM_NAME = "notAllowed";
  public static final String CONFLICT_STREAM_NAME = "conflict";
  public static final String WRITER_TEST_STREAM_NAME_POSTFIX = "WriterTest";
  public static final String FILE_STREAM_NAME = "file";
  public static final String WITH_CUSTOM_HEADER_STREAM_NAME = "withHeader";

  private TestUtils() {
  }

  public static String getStreamNameFromUri(String uri) {
    String streamName = StringUtils.EMPTY;
    if (StringUtils.isNotEmpty(uri)) {
      Pattern p = Pattern.compile("streams/.*?/");
      Matcher m = p.matcher(uri);
      if (m.find()) {
        String b = m.group();
        streamName = b.substring(b.indexOf("/") + 1, b.length() - 1);
      }
    }
    return streamName;
  }

  public static int getStatusCodeByStreamName(String streamName) {
    int code;
    if (StringUtils.isEmpty(streamName)) {
      code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    } else if (SUCCESS_STREAM_NAME.equals(streamName) || TestUtils.FILE_STREAM_NAME.equals(streamName)) {
      code = HttpStatus.SC_OK;
    } else if (NOT_FOUND_STREAM_NAME.equals(streamName)) {
      code = HttpStatus.SC_NOT_FOUND;
    } else if (BAD_REQUEST_STREAM_NAME.endsWith(streamName)) {
      code = HttpStatus.SC_BAD_REQUEST;
    } else if (FORBIDDEN_STREAM_NAME.equals(streamName)) {
      code = HttpStatus.SC_FORBIDDEN;
    } else if (NOT_ALLOWED_STREAM_NAME.equals(streamName)) {
      code = HttpStatus.SC_METHOD_NOT_ALLOWED;
    } else if (CONFLICT_STREAM_NAME.equals(streamName)) {
      code = HttpStatus.SC_CONFLICT;
    } else {
      code = HttpStatus.SC_NOT_IMPLEMENTED;
    }
    return code;
  }

  public static int authorize(HttpRequest httpRequest) {
    int statusCode;
    Header authHeader = httpRequest.getFirstHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || StringUtils.isEmpty(authHeader.getValue()) ||
      !authHeader.getValue().equals("Bearer " + RestTest.AUTH_TOKEN)) {
      statusCode = HttpStatus.SC_UNAUTHORIZED;
    } else {
      statusCode = HttpStatus.SC_OK;
    }
    return statusCode;
  }

}
