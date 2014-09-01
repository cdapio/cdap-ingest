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
import co.cask.cdap.client.auth.AuthenticationClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The basic implementation of the authentication client to fetch the access token from the Auth. Server through
 * the REST API with username and password.
 */
public class BasicAuthenticationClient implements AuthenticationClient<BasicCredentials> {
  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticationClient.class);

  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String AUTH_URI_KEY = "auth_uri";
  private static final String AUTHENTICATION_HEADER_PREFIX_BASIC = "Basic ";
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";

  private final HttpClient httpClient;
  private URI baseUrl;
  private URI authUrl;

  public BasicAuthenticationClient() {
    this.httpClient = HttpClients.custom().build();
  }

  @Override
  public void configure(String host, int port, boolean ssl) {
    baseUrl = URI.create(String.format("%s://%s:%d", ssl ? HTTPS_PROTOCOL : HTTP_PROTOCOL, host, port));
  }

  @Override
  public String getAccessToken(BasicCredentials credentials) throws IOException {
    if (!isAuthEnabled()) {
      throw new IOException("Authentication is disabled in the gateway server.");
    }

    String username = credentials.getUsername();
    String password = credentials.getPassword();
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      throw new IOException("Username or password argument is empty.");
    }

    LOG.debug("Authentication is enabled in the gateway server. Authentication URI {}.", authUrl);
    HttpGet getRequest = new HttpGet(authUrl);

    String auth = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes());
    auth = auth.replaceAll("(\r|\n)", StringUtils.EMPTY);
    getRequest.addHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_HEADER_PREFIX_BASIC + auth);

    HttpResponse httpResponse = httpClient.execute(getRequest);
    RestClientUtils.verifyResponseCode(httpResponse);
    JsonObject jsonContent = RestClientUtils.toJsonObject(httpResponse.getEntity());
    return jsonContent.get(ACCESS_TOKEN_KEY).getAsString();
  }

  @Override
  public boolean isAuthEnabled() throws IOException {
    boolean isAuthEnabled = false;
    String strAuthUrl = getAuthURL();
    if (StringUtils.isNotEmpty(strAuthUrl)) {
      isAuthEnabled = true;
      authUrl = URI.create(strAuthUrl);
    }
    return isAuthEnabled;
  }

  private String getAuthURL() throws IOException {
    LOG.debug("Try to get the authentication URI from the gateway server: {}.", baseUrl);
    String result = StringUtils.EMPTY;
    HttpGet get = new HttpGet(baseUrl);
    HttpResponse response = httpClient.execute(get);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
      JsonObject responseJson = RestClientUtils.toJsonObject(response.getEntity());
      JsonArray addresses = responseJson.get(AUTH_URI_KEY).getAsJsonArray();
      List<String> list = new ArrayList<String>();
      for (JsonElement e : addresses) {
        list.add(e.getAsString());
      }
      result = list.get(new Random().nextInt(list.size()));
    } else {
      RestClientUtils.verifyResponseCode(response);
    }
    return result;
  }
}
