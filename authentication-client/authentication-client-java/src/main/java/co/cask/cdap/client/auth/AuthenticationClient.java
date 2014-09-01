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
 * @param <T> Type of the object which contains credentials required for the enabled authentication provider in the
 * authentication server.
 */
public interface AuthenticationClient<T extends Credentials> {
  /**
   * Configures the address of the gateway server for the authentication client.
   *
   * @param host gateway server host name
   * @param port gateway server port
   * @param ssl  should be true, if SSL is enabled in the gateway server.
   */
  void configure(String host, int port, boolean ssl);

  /**
   * Retrieves the access token generated according to the credentials required by thr authentication provider
   * in the authentication server.
   *
   * @param credentials object contains credentials according to the enabled  authentication provider in the
   *                    authentication server
   * @return String value of the access token
   * @throws IOException in case of a problem or the connection was aborted or authentication is disabled in the
   *                     gateway server
   */
  String getAccessToken(T credentials) throws IOException;

  /**
   * Checks is the authentication enabled in the gateway server.
   *
   * @return true if authentication is enabled
   * @throws IOException in case of a problem or the connection was aborted
   */
  boolean isAuthEnabled() throws IOException;
}
