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
 * This class contains all info about credential supported by the authentication server.
 */
public class Credential {
  private final String name;
  private final String description;
  private final boolean secret;

  /**
   * Constructs new instance.
   *
   * @param name the credential consists of name
   * @param description the full description of the credential
   * @param secret true, if this credential field is secret
   */
  public Credential(String name, String description, boolean secret) {
    this.name = name;
    this.description = description;
    this.secret = secret;
  }

  /**
   * @return the credential consists of name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the full description of the credential
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return true, if this credential field is secret
   */
  public boolean isSecret() {
    return secret;
  }
}
