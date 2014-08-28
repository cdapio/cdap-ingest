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
 * The client interface to fetch the access token from the Auth. Server.
 */
public interface AuthenticationClient {
  /**
   * Retrieves the access token generated according to the credentials required by Auth. Provider in the Auth. Server.
   *
   * @return String value of the access token
   */
  String getAccessToken() throws IOException;
}
