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

import java.io.IOException;
import java.util.Properties;

/**
 * The client interface to fetch access token from the authentication server.
 */
public interface AuthenticationClient {
  /**
   * Configures the authentication client and can be called only once for every AuthenticationClient object
   *
   * @param properties the configuration for authentication client which includes connection information and
   *                   credentials
   */
  void configure(Properties properties);

  /**
   * Retrieves the access token generated according to the credentials required by the authentication provider
   * in the authentication server. The access token will be cached until its expiry.
   *
   * @return {@link AccessToken} object containing the access token
   * @throws IOException in case of a problem or the connection was aborted or authentication is disabled in the
   *                     gateway server
   */
  AccessToken getAccessToken() throws IOException;

  /**
   * Checks if authentication is enabled on the gateway server.
   *
   * @return true if authentication is enabled
   * @throws IOException in case of a problem or the connection was aborted
   */
  boolean isAuthEnabled() throws IOException;

  /**
   * Invalidate the cached access token.
   */
  void invalidateToken();
}
