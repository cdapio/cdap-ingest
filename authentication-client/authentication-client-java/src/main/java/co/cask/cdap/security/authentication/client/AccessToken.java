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

/**
 * This class represents access token object.
 */
public class AccessToken {
  private final String value;
  private final Long expiresIn;
  private final String tokenType;

  /**
   * Constructs new instance of the access token.
   *
   * @param value     string value of the access token
   * @param expiresIn token validity lifetime in seconds
   * @param tokenType access token value supported by the authentication provider in the authentication server.
   */
  public AccessToken(String value, Long expiresIn, String tokenType) {
    this.value = value;
    this.expiresIn = expiresIn;
    this.tokenType = tokenType;
  }

  public String getValue() {
    return value;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }
}
