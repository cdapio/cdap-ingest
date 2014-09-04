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

import co.cask.cdap.client.rest.RestTest;
import co.cask.cdap.client.rest.TestUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/**
 * The http request handler implementation to test client's requests to the provide information method in the REST
 * Stream API.
 */
public class StreamInfoHttpRequestHandler implements HttpRequestHandler {

  @Override
  public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext)
    throws HttpException, IOException {

    RequestLine requestLine = httpRequest.getRequestLine();
    String method = requestLine.getMethod();
    int statusCode;
    if (!HttpMethod.GET.equals(method)) {
      statusCode = HttpStatus.SC_NOT_IMPLEMENTED;
    } else {
      String uri = requestLine.getUri();
      String streamName = TestUtils.getStreamNameFromUri(uri);
      if (TestUtils.AUTH_STREAM_NAME.equals(streamName)) {
        statusCode = TestUtils.authorize(httpRequest);
      } else if (streamName.contains(TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX)) {
        statusCode = HttpStatus.SC_OK;
      } else {
        statusCode = TestUtils.getStatusCodeByStreamName(streamName);
      }
      if (statusCode == HttpStatus.SC_OK) {
        StringEntity entity = new StringEntity("{\"partitionDuration\":3600000,\"indexInterval\":10000,\"ttl\":"
                                                 + RestTest.STREAM_TTL + "}");
        entity.setContentType(MediaType.APPLICATION_JSON);
        response.setEntity(entity);
      }
    }
    response.setStatusCode(statusCode);
  }
}
