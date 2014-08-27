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

package co.cask.cdap.client.auth;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The REST client abstract class to fetch the access token from the Auth. Server.
 */
public abstract class RestAuthenticationClient implements AuthenticationClient {
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";

  protected final String host;
  protected final int port;
  protected final boolean ssl;
  protected final URI baseUrl;

  protected RestAuthenticationClient(String host, int port) throws URISyntaxException {
    this(host, port, false);
  }

  protected RestAuthenticationClient(String host, int port, boolean ssl) throws URISyntaxException {
    this.host = host;
    this.port = port;
    this.ssl = ssl;
    this.baseUrl = new URI(String.format("%s://%s:%d", ssl ? HTTPS_PROTOCOL : HTTP_PROTOCOL, host, port));
  }

  @Override
  public abstract String getAccessToken();

  /**
   * @return the base URL of the Rest Service API
   */
  public URI getBaseURL() {
    return baseUrl;
  }
}
