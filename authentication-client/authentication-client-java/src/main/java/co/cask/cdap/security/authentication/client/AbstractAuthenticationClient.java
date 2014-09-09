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


package co.cask.cdap.security.authentication.client;

import co.cask.cdap.security.authentication.client.basic.RestClientUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Abstract authentication client implementation with common methods.
 */
public abstract class AbstractAuthenticationClient implements AuthenticationClient {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthenticationClient.class);

  private static final Random RANDOM = new Random();
  private static final String AUTH_URI_KEY = "auth_uri";
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String EXPIRES_IN_KEY = "expires_in";
  private static final String TOKEN_TYPE_KEY = "token_type";
  private static final long SPARE_TIME_IN_MILLIS = 5000;
  private static final Gson GSON = new Gson();

  private long expirationTime;
  private AccessToken accessToken;
  private URI baseUrl;
  private URI authUrl;
  private Boolean authEnabled;
  private final HttpClient httpClient;

  /**
   * Fetches the access token from the authentication server.
   *
   * @return {@link AccessToken} object containing the access token
   * @throws IOException in case of a problem or the connection was aborted or if the access token is not received
   * successfully form the authentication server
   */
  protected abstract AccessToken fetchAccessToken() throws IOException;

  /**
   * Constructs new instance.
   */
  protected AbstractAuthenticationClient() {
    this.httpClient = new DefaultHttpClient();
  }

  @Override
  public void invalidateToken() {
    accessToken = null;
  }

  @Override
  public boolean isAuthEnabled() throws IOException {
    if (authEnabled == null) {
      String strAuthUrl = fetchAuthURL();
      authEnabled = StringUtils.isNotEmpty(strAuthUrl);
      if (authEnabled) {
        authUrl = URI.create(strAuthUrl);
      }
    }
    return authEnabled;
  }

  @Override
  public void setConnectionInfo(String host, int port, boolean ssl) {
    if (baseUrl != null) {
      throw new IllegalStateException("Connection info is already configured!");
    }
    baseUrl = URI.create(String.format("%s://%s:%d", ssl ? HTTPS_PROTOCOL : HTTP_PROTOCOL, host, port));
  }

  @Override
  public AccessToken getAccessToken() throws IOException {
    if (!isAuthEnabled()) {
      throw new IOException("Authentication is disabled in the gateway server.");
    }

    if (accessToken == null || isTokenExpired()) {
      long requestTime = System.currentTimeMillis();
      accessToken = fetchAccessToken();
      expirationTime = requestTime + TimeUnit.SECONDS.toMillis(accessToken.getExpiresIn()) - SPARE_TIME_IN_MILLIS;
      LOG.debug("Received the access token successfully. Expiration date is {}.", new Date(expirationTime));
    }
    return accessToken;
  }

  /**
   * Checks if the access token has expired.
   *
   * @return true, if the access token has expired
   */
  private boolean isTokenExpired() {
    return expirationTime < System.currentTimeMillis();
  }

  /**
   * Fetches the available authentication server URL, if authentication is enabled in the gateway server,
   * otherwise, empty string will be returned.
   *
   * @return string value of the authentication server URL
   * @throws IOException IOException in case of a problem or the connection was aborted or if url list is empty
   */
  private String fetchAuthURL() throws IOException {
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
        throw new IOException("Authentication servers list is empty.");
      }
    }
    return result;
  }

  /**
   * Executes fetch access token request.
   *
   * @param request the http request to fetch access token from the authentication server
   * @return  {@link AccessToken} object containing the access token
   * @throws IOException IOException in case of a problem or the connection was aborted or if the access token is not
   * received successfully from the authentication server
   */
  protected AccessToken execute(HttpRequestBase request) throws IOException {
    HttpResponse httpResponse = httpClient.execute(request);
    RestClientUtils.verifyResponseCode(httpResponse);

    Map<String, String> responseMap = GSON.fromJson(EntityUtils.toString(httpResponse.getEntity()),
                                                    new TypeToken<Map<String, String>>() { }.getType());
    String tokenValue = responseMap.get(ACCESS_TOKEN_KEY);
    String tokenType = responseMap.get(TOKEN_TYPE_KEY);
    String expiresInStr = responseMap.get(EXPIRES_IN_KEY);

    if (StringUtils.isEmpty(tokenValue) || StringUtils.isEmpty(tokenType) || StringUtils.isEmpty(expiresInStr)) {
      throw new IOException("Unexpected response was received from the authentication server.");
    }

    return new AccessToken(tokenValue, Long.valueOf(expiresInStr), tokenType);
  }

  /**
   * @return the authentication server URL or empty value if authentication is not enabled in the gateway server
   */
  protected URI getAuthUrl() {
    return authUrl;
  }
}
