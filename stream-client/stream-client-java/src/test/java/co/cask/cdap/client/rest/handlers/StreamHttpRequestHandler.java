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

package co.cask.cdap.client.rest.handlers;

import co.cask.cdap.client.rest.RestClient;
import co.cask.cdap.client.rest.RestTest;
import co.cask.cdap.client.rest.TestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import javax.ws.rs.HttpMethod;

public class StreamHttpRequestHandler implements HttpRequestHandler {
  @Override
  public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext)
    throws HttpException, IOException {

    RequestLine requestLine = httpRequest.getRequestLine();
    String method = requestLine.getMethod();
    int statusCode;
    String uri = requestLine.getUri();
    String streamName = TestUtils.getStreamNameFromUri(uri + "/");
    if (HttpMethod.PUT.equals(method)) {
      if (TestUtils.AUTH_STREAM_NAME.equals(streamName)) {
        statusCode = TestUtils.authorize(httpRequest);
      } else {
        statusCode = TestUtils.getStatusCodeByStreamName(streamName);
      }
    } else if (HttpMethod.POST.equals(method)) {
      String fullStreamName = streamName;
      streamName = streamName.replace(TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX, StringUtils.EMPTY);
      if (TestUtils.AUTH_STREAM_NAME.equals(streamName)) {
        statusCode = TestUtils.authorize(httpRequest);
      } else if (TestUtils.WITH_CUSTOM_HEADER_STREAM_NAME.endsWith(streamName)) {
        Header testHeader = httpRequest.getFirstHeader(fullStreamName + "." + RestTest.TEST_HEADER_NAME);
        if (testHeader != null && RestTest.TEST_HEADER_VALUE.equals(testHeader.getValue())) {
          statusCode = HttpStatus.SC_OK;
        } else {
          statusCode = HttpStatus.SC_BAD_REQUEST;
        }
      } else {
        statusCode = TestUtils.getStatusCodeByStreamName(streamName);
      }
      if (HttpStatus.SC_OK == statusCode && !TestUtils.FILE_STREAM_NAME.equals(streamName)) {
        //check request content
        BasicHttpEntityEnclosingRequest request = (BasicHttpEntityEnclosingRequest) httpRequest;
        HttpEntity requestEntity = request.getEntity();
        if (requestEntity != null) {
          String content = RestClient.getEntityAsString(requestEntity);
          if (StringUtils.isEmpty(content) || !RestTest.EXPECTED_WRITER_CONTENT.equals(content)) {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
          }
        } else {
          statusCode = HttpStatus.SC_BAD_REQUEST;
        }
      }
    } else {
      statusCode = HttpStatus.SC_NOT_IMPLEMENTED;
    }
    response.setStatusCode(statusCode);
  }
}
