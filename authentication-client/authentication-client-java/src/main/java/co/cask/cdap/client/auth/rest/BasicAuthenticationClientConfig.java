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


package co.cask.cdap.client.auth.rest;

import co.cask.cdap.client.auth.AuthenticationClientConfig;

/**
 * This class provides configurations for the Basic Authentication Client.
 */
public class BasicAuthenticationClientConfig implements AuthenticationClientConfig {
  private final String hostname;
  private final int port;
  private final boolean ssl;

  /**
   * Constructs new instance.
   *
   * @param hostname the gateway server host name
   * @param port the gateway server port
   * @param ssl true if SSL is enabled in the gateway server
   */
  public BasicAuthenticationClientConfig(String hostname, int port, boolean ssl) {
    this.hostname = hostname;
    this.port = port;
    this.ssl = ssl;
  }

  /**
   * @return the gateway server hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @return the gateway server port name
   */
  public int getPort() {
    return port;
  }

  /**
   * @return the true value if SSL is enabled
   */
  public boolean isSSL() {
    return ssl;
  }
}
