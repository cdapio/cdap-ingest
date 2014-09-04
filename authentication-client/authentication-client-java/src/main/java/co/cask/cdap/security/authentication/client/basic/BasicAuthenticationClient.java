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

package co.cask.cdap.security.authentication.client.basic;

import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * The basic implementation of the non-interactive authentication client to fetch the access token from the
 * authentication server through the REST API with username and password.
 */
public class BasicAuthenticationClient implements AuthenticationClient {

  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticationClient.class);

  private static final Gson GSON = new Gson();
  private static final Random RANDOM = new Random();
  private static final long SPARE_TIME_IN_MILLIS = 5000;
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String EXPIRES_IN_KEY = "expires_in";
  private static final String TOKEN_TYPE_KEY = "token_type";
  private static final String AUTH_URI_KEY = "auth_uri";
  private static final String AUTHENTICATION_HEADER_PREFIX_BASIC = "Basic ";
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";
  private static final String USERNAME_PROP_NAME = "security.auth.client.username";
  private static final String PASSWORD_PROP_NAME = "security.auth.client.password";
  private static final String HOSTNAME_PROP_NAME = "security.auth.client.gateway.hostname";
  private static final String PORT_PROP_NAME = "security.auth.client.gateway.port";
  private static final String SSL_PROP_NAME = "security.auth.client.gateway.ssl.enabled";

  private final HttpClient httpClient;
  private URI baseUrl;
  private URI authUrl;
  private Boolean authEnabled;
  private AccessToken accessToken;
  private String username;
  private String password;
  private long expirationTime;

  public BasicAuthenticationClient() {
    this.httpClient = new DefaultHttpClient();
  }

  @Override
  public void configure(Properties properties) {
    if (baseUrl != null) {
      throw new IllegalStateException("Client is already configured!");
    } else {
      username = properties.getProperty(USERNAME_PROP_NAME);
      Preconditions.checkArgument(StringUtils.isNotEmpty(username), "The username property cannot be empty.");

      password = properties.getProperty(PASSWORD_PROP_NAME);
      Preconditions.checkArgument(StringUtils.isNotEmpty(password), "The password property cannot be empty.");

      String hostname = properties.getProperty(HOSTNAME_PROP_NAME);
      Preconditions.checkArgument(StringUtils.isNotEmpty(hostname),
                                  "The gateway server hostname property cannot be empty.");

      String port = properties.getProperty(PORT_PROP_NAME);
      Preconditions.checkArgument(StringUtils.isNotEmpty(port), "The gateway server port property cannot be empty.");

      Boolean isSslEnable = Boolean.valueOf(properties.getProperty(SSL_PROP_NAME, "false"));

      baseUrl = URI.create(String.format("%s://%s:%d", isSslEnable ? HTTPS_PROTOCOL : HTTP_PROTOCOL, hostname,
                                         Integer.valueOf(port)));
      LOG.debug("Basic authentication client for the gateway server: {} is configured successfully.", baseUrl);
    }
  }

  @Override
  public AccessToken getAccessToken() throws IOException {
    if (!isAuthEnabled()) {
      throw new IOException("Authentication is disabled in the gateway server.");
    }

    if (accessToken == null || isTokenExpired()) {
      accessToken = fetchAccessToken();
    }
    return accessToken;
  }

  @Override
  public void invalidateToken() {
    accessToken = null;
  }

  @Override
  public boolean isAuthEnabled() throws IOException {
    if (authEnabled == null) {
      String strAuthUrl = getAuthURL();
      authEnabled = StringUtils.isNotEmpty(strAuthUrl);
      if (authEnabled) {
        authUrl = URI.create(strAuthUrl);
      }
    }
    return authEnabled;
  }

  private AccessToken fetchAccessToken() throws IOException {
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      throw new IllegalStateException("Base authentication client is not configured!");
    }

    long requestTime = System.currentTimeMillis();
    LOG.debug("Authentication is enabled in the gateway server. Authentication URI {}.", authUrl);
    HttpGet getRequest = new HttpGet(authUrl);

    String auth = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes());
    auth = auth.replaceAll("(\r|\n)", StringUtils.EMPTY);
    getRequest.addHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_HEADER_PREFIX_BASIC + auth);

    HttpResponse httpResponse = httpClient.execute(getRequest);
    RestClientUtils.verifyResponseCode(httpResponse);

    Map<String, String> responseMap = GSON.fromJson(EntityUtils.toString(httpResponse.getEntity()),
            new TypeToken<Map<String, String>>() { }.getType());
    String tokenValue = responseMap.get(ACCESS_TOKEN_KEY);
    String tokenType = responseMap.get(TOKEN_TYPE_KEY);
    String expiresInStr = responseMap.get(EXPIRES_IN_KEY);
    if (StringUtils.isEmpty(tokenValue) || StringUtils.isEmpty(tokenType) || StringUtils.isEmpty(expiresInStr)) {
      throw new IOException("Unexpected response was received from the authentication server.");
    }

    Long expiresIn = Long.valueOf(expiresInStr);
    expirationTime = requestTime + TimeUnit.SECONDS.toMillis(expiresIn) - SPARE_TIME_IN_MILLIS;
    return new AccessToken(tokenValue, expiresIn, tokenType);
  }

  private boolean isTokenExpired() {
    return expirationTime < System.currentTimeMillis();
  }

  private String getAuthURL() throws IOException {
    if (baseUrl == null) {
      throw new IllegalStateException("Base authentication client is not configured!");
    }

    LOG.debug("Try to get the authentication URI from the gateway server: {}.", baseUrl);
    String result = StringUtils.EMPTY;
    HttpGet get = new HttpGet(baseUrl);
    HttpResponse response = httpClient.execute(get);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
      Map<String, List<String>> responseMap = GSON.fromJson(EntityUtils.toString(response.getEntity()),
                                                            new TypeToken<Map<String, List<String>>>() { }.getType());
      List<String> uriList = responseMap.get(AUTH_URI_KEY);
      if (uriList != null && !uriList.isEmpty()) {
        result = uriList.get(RANDOM.nextInt(uriList.size()));
      } else {
        throw new IOException("Authenticated url is not available from the gateway server.");
      }
    }
    return result;
  }
}
