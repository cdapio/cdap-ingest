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

import java.io.IOException;

/**
 * The client interface to fetch access tokens from the authentication server.
 *
 * @param <T> Type of the object which contains configuration parameters for the client
 * @param <K> Type of the object which contains credentials required for the enabled authentication provider in the
 * authentication server.
 */
public interface AuthenticationClient<T extends AuthenticationClientConfig, K extends Credentials> {
  /**
   * Configures the address of the gateway server for the authentication client.
   *
   * @param config contains gateway server access parameters
   *
   */
  void configure(T config);

  /**
   * Method for set credentials to the client instance
   *
   * @param credentials contains credentials corresponding to enabled authentication provider in the
   *                    authentication server
   */
  void setCredentials(K credentials);

  /**
   * Retrieves the access token generated according to the credentials required by thr authentication provider
   * in the authentication server.
   *
   * @return String value of the access token
   * @throws IOException in case of a problem or the connection was aborted or authentication is disabled in the
   *                     gateway server
   */
  String getAccessToken() throws IOException;

  /**
   * Checks is the authentication enabled in the gateway server.
   *
   * @return true if authentication is enabled
   * @throws IOException in case of a problem or the connection was aborted
   */
  boolean isAuthEnabled() throws IOException;

  /**
   * Invalidate cashed access token.
   *
   * @return the new access token string value
   */
  String invalidateToken() throws IOException;
}
