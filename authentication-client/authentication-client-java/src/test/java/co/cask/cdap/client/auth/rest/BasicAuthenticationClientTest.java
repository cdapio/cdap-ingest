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

import co.cask.cdap.client.auth.AuthenticationClient;
import co.cask.cdap.client.auth.Credentials;
import co.cask.cdap.client.auth.rest.handlers.AuthenticationHandler;
import co.cask.cdap.client.auth.rest.handlers.BaseHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.localserver.LocalTestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.assertEquals;


public class BasicAuthenticationClientTest {
  public static final String AUTHENTICATED_USERNAME = "admin";
  public static final String AUTHENTICATED_PASSWORD = "realtime";
  public static final String TOKEN = "SuccessGeneratedToken";

  private AuthenticationClient<BasicCredentials> authenticationClient;

  private LocalTestServer localTestServer;
  private String testServerHost;
  private int testServerPort;
  private final AuthenticationHandler authenticationHandler = new AuthenticationHandler();
  private final BaseHandler baseHandler = new BaseHandler();
  private BasicCredentials credentials;

  @Before
  public void setUp() throws Exception {
    localTestServer = new LocalTestServer(null, null);
    localTestServer.register("*/token", authenticationHandler);
    localTestServer.register("*", baseHandler);
    localTestServer.start();
    testServerHost = localTestServer.getServiceAddress().getHostName();
    testServerPort = localTestServer.getServiceAddress().getPort();
    baseHandler.setAuthHost(testServerHost);
    baseHandler.setAuthPort(testServerPort);
    authenticationClient = new BasicAuthenticationClient();
    authenticationClient.configure(testServerHost, testServerPort, false);
  }

  @Test
  public void testSuccessGetAccessToken() throws IOException {
    credentials = new BasicCredentials(AUTHENTICATED_USERNAME, AUTHENTICATED_PASSWORD);
    assertEquals(TOKEN, authenticationClient.getAccessToken(credentials));
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedGetAccessToken() throws IOException {
    credentials = new BasicCredentials("test", "test");
    authenticationClient.getAccessToken(credentials);
  }

  @Test(expected = IOException.class)
  public void testEmptyUsernameGetAccessToken() throws IOException {
    credentials = new BasicCredentials(StringUtils.EMPTY, "test");
    authenticationClient.getAccessToken(credentials);
  }

  @Test(expected = IOException.class)
  public void testEmptyPassGetAccessToken() throws IOException {
    credentials = new BasicCredentials("test", StringUtils.EMPTY);
    authenticationClient.getAccessToken(credentials);
  }

  @After
  public void shutDown() throws Exception {
    localTestServer.stop();
  }
}
