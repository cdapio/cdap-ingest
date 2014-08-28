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

package co.cask.cdap.client.auth.rest;

import co.cask.cdap.client.auth.rest.handlers.AuthenticationHandler;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;

public class RestAuthenticationClientTest {
  public static final String AUTHENTICATED_USERNAME = "admin";
  public static final String AUTHENTICATED_PASSWORD = "realtime";
  public static final String TOKEN = "SuccessGeneratedToken";

  private LocalTestServer localTestServer;

  protected String testServerHost;
  protected int testServerPort;

  private final HttpRequestHandler authenticationHandler = new AuthenticationHandler();

  @Before
  public void setUp() throws Exception {
    localTestServer = new LocalTestServer(null, null);
    localTestServer.register("*/token", authenticationHandler);
    localTestServer.start();
    testServerHost = localTestServer.getServiceAddress().getHostName();
    testServerPort = localTestServer.getServiceAddress().getPort();
  }

  @After
  public void shutDown() throws Exception {
    localTestServer.stop();
  }
}
