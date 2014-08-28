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

package co.cask.cdap.client.auth.rest;

import co.cask.cdap.client.RestClientUtils;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URI;

/**
 * The basic implementation of the authentication client to fetch the access token from the Auth. Server through
 * the REST API with username and password.
 */
public class BasicRestAuthenticationClient extends RestAuthenticationClient {
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String AUTHENTICATION_HEADER_PREFIX_BASIC = "Basic ";

  private final String username;
  private final String password;

  public BasicRestAuthenticationClient(String host, int port, boolean ssl, String username, String password) {
    super(host, port, ssl);
    this.username = username;
    this.password = password;
  }

  public BasicRestAuthenticationClient(String host, int port, String username, String password) {
    this(host, port, false, username, password);
  }

  @Override
  public String getAccessToken() throws IOException {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      throw new IOException("Username or password argument is empty.");
    }

    HttpGet getRequest = new HttpGet(URI.create(authServerBaseUrl + "/token"));
    String auth = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes());
    auth = auth.replaceAll("(\r|\n)", StringUtils.EMPTY);
    getRequest.addHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_HEADER_PREFIX_BASIC + auth);

    HttpResponse httpResponse = httpClient.execute(getRequest);
    RestClientUtils.responseCodeAnalysis(httpResponse);
    JsonObject jsonContent = RestClientUtils.toJsonObject(httpResponse.getEntity());
    return jsonContent.get(ACCESS_TOKEN_KEY).getAsString();
  }
}
