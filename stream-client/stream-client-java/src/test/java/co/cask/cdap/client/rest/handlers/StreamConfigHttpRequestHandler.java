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
import co.cask.cdap.client.rest.RestStreamClient;
import co.cask.cdap.client.rest.RestTest;
import co.cask.cdap.client.rest.TestUtils;
import com.google.gson.JsonObject;
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

public class StreamConfigHttpRequestHandler implements HttpRequestHandler {
  @Override
  public void handle(HttpRequest httpRequest, HttpResponse response, HttpContext httpContext)
    throws HttpException, IOException {

    RequestLine requestLine = httpRequest.getRequestLine();
    String method = requestLine.getMethod();
    int statusCode;
    if (!HttpMethod.PUT.equals(method)) {
      statusCode = HttpStatus.SC_NOT_IMPLEMENTED;
    } else {
      String uri = requestLine.getUri();
      String streamName = TestUtils.getStreamNameFromUri(uri);
      if (TestUtils.SUCCESS_STREAM_NAME.equals(streamName)) {
        statusCode = HttpStatus.SC_BAD_REQUEST;
        BasicHttpEntityEnclosingRequest request = (BasicHttpEntityEnclosingRequest) httpRequest;
        HttpEntity requestEntity = request.getEntity();
        if (requestEntity != null) {
          JsonObject jsonContent = RestClient.getEntityAsJsonObject(requestEntity);
          if (jsonContent != null) {
            long ttl = jsonContent.get(RestStreamClient.TTL_ATTRIBUTE_NAME).getAsLong();
            if (ttl == RestTest.STREAM_TTL) {
              statusCode = HttpStatus.SC_OK;
            }
          }
        }
      } else if (TestUtils.AUTH_STREAM_NAME.equals(streamName)) {
        statusCode = TestUtils.authorize(httpRequest);
      } else {
        statusCode = TestUtils.getStatusCodeByStreamName(streamName);
      }
    }
    response.setStatusCode(statusCode);
  }
}
