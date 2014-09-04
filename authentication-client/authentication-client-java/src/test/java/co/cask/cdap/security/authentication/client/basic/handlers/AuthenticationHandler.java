/*
 * Copyright 2014 Cask Data, Inc.
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

package co.cask.cdap.security.authentication.client.basic.handlers;

import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClientTest;
import com.google.common.base.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
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

public class AuthenticationHandler implements HttpRequestHandler {
  @Override
  public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
    throws HttpException, IOException {

    RequestLine requestLine = httpRequest.getRequestLine();
    String method = requestLine.getMethod();
    int statusCode;
    if (HttpMethod.GET.equals(method)) {
      String authHeaderVal = httpRequest.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue();
      if (StringUtils.isNotEmpty(authHeaderVal)) {
        authHeaderVal = authHeaderVal.replace("Basic ", StringUtils.EMPTY);
        String credentialsStr = new String(Base64.decodeBase64(authHeaderVal), Charsets.UTF_8);
        String[] credentials = credentialsStr.split(":");
        String username = credentials[0];
        String password = credentials[1];
        if (BasicAuthenticationClientTest.USERNAME.equals(username) &&
          BasicAuthenticationClientTest.PASSWORD.equals(password)) {
          StringEntity entity = new StringEntity(createEntityBody(BasicAuthenticationClientTest.TOKEN,
                                                                  BasicAuthenticationClientTest.TOKEN_TYPE,
                                                                  BasicAuthenticationClientTest.TOKEN_LIFE_TIME));
          entity.setContentType(MediaType.APPLICATION_JSON);
          httpResponse.setEntity(entity);
          statusCode = HttpStatus.SC_OK;
        } else if (BasicAuthenticationClientTest.EMPTY_TOKEN_USERNAME.equals(username)) {
          StringEntity entity = new StringEntity(createEntityBody(StringUtils.EMPTY,
                                                                  BasicAuthenticationClientTest.TOKEN_TYPE,
                                                                  BasicAuthenticationClientTest.TOKEN_LIFE_TIME));
          entity.setContentType(MediaType.APPLICATION_JSON);
          httpResponse.setEntity(entity);
          statusCode = HttpStatus.SC_OK;
        } else {
          statusCode = HttpStatus.SC_UNAUTHORIZED;
        }
      } else {
        statusCode = HttpStatus.SC_BAD_REQUEST;
      }
    } else {
      statusCode = HttpStatus.SC_NOT_IMPLEMENTED;
    }
    httpResponse.setStatusCode(statusCode);
  }

  private static String createEntityBody(String value, String type, Long expiresIn) {
    return "{'access_token':'" + value + "','token_type':'" + type + "','expires_in':" + expiresIn + "}";
  }
}
