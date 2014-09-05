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

package co.cask.cdap.security.authentication.client.basic;

import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.handlers.AuthDisabledHandler;
import co.cask.cdap.security.authentication.client.basic.handlers.AuthenticationHandler;
import co.cask.cdap.security.authentication.client.basic.handlers.BaseHandler;
import co.cask.cdap.security.authentication.client.basic.handlers.EmptyUrlListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.localserver.LocalTestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class BasicAuthenticationClientTest {
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "realtime";
  public static final String TOKEN = "SuccessGeneratedToken";
  public static final String NEW_TOKEN = "SuccessGeneratedSecondToken";
  public static final String TOKEN_TYPE = "Bearer";
  public static final String EMPTY_TOKEN_USERNAME = "emptyToken";
  public static final String EXPIRED_TOKEN_USERNAME = "expiredToken";
  public static final Long TOKEN_LIFE_TIME = 86400L;
  private static final String USERNAME_PROP_NAME = "security.auth.client.username";
  private static final String PASSWORD_PROP_NAME = "security.auth.client.password";

  private AuthenticationClient authenticationClient;

  private LocalTestServer localTestServer;
  private Properties testProperties;
  private final AuthenticationHandler authenticationHandler = new AuthenticationHandler();
  private final BaseHandler baseHandler = new BaseHandler();

  @Before
  public void setUp() throws Exception {
    authenticationClient = new BasicAuthenticationClient();
    localTestServer = new LocalTestServer(null, null);
    localTestServer.register("*/token", authenticationHandler);
    localTestServer.register("*", baseHandler);
    localTestServer.start();
    String testServerHost = localTestServer.getServiceAddress().getHostName();
    int testServerPort = localTestServer.getServiceAddress().getPort();
    baseHandler.setAuthHost(testServerHost);
    baseHandler.setAuthPort(testServerPort);
    testProperties = new Properties();
    authenticationClient.setConnectionInfo(testServerHost, testServerPort, false);
    testProperties.setProperty(USERNAME_PROP_NAME, USERNAME);
    testProperties.setProperty(PASSWORD_PROP_NAME, PASSWORD);
  }

  @Test
  public void testSuccessGetAccessToken() throws IOException {
    authenticationClient.configure(testProperties);
    AccessToken accessToken = authenticationClient.getAccessToken();
    assertTrue(accessToken != null);
    assertEquals(TOKEN, accessToken.getValue());
    assertEquals(TOKEN_TYPE, accessToken.getTokenType());
    assertEquals(TOKEN_LIFE_TIME, accessToken.getExpiresIn());
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedGetAccessToken() throws IOException {
    testProperties.setProperty(USERNAME_PROP_NAME, "test");
    testProperties.setProperty(PASSWORD_PROP_NAME, "test");
    authenticationClient.configure(testProperties);
    authenticationClient.getAccessToken();
  }

  @Test(expected = IOException.class)
  public void testEmptyTokenGetAccessToken() throws IOException {
    testProperties.setProperty(USERNAME_PROP_NAME, EMPTY_TOKEN_USERNAME);
    authenticationClient.configure(testProperties);
    authenticationClient.getAccessToken();
  }

  @Test
  public void testExpiredTokenGetAccessToken() throws IOException {
    testProperties.setProperty(USERNAME_PROP_NAME, EXPIRED_TOKEN_USERNAME);
    authenticationClient.configure(testProperties);
    AccessToken accessToken = authenticationClient.getAccessToken();
    assertEquals(TOKEN, accessToken.getValue());
    accessToken = authenticationClient.getAccessToken();
    assertTrue(accessToken != null);
    assertEquals(NEW_TOKEN, accessToken.getValue());
    assertEquals(TOKEN_TYPE, accessToken.getTokenType());
  }

  @Test(expected = IllegalStateException.class)
  public void testNotConfigureGetAccessToken() throws IOException {
    authenticationClient.getAccessToken();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUsernameConfigure() throws IOException {
    testProperties.setProperty(USERNAME_PROP_NAME, StringUtils.EMPTY);
    authenticationClient.configure(testProperties);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPassConfigure() throws IOException {
    testProperties.setProperty(PASSWORD_PROP_NAME, StringUtils.EMPTY);
    authenticationClient.configure(testProperties);
  }

  @Test(expected = IllegalStateException.class)
  public void testSecondCallConfigure() throws IOException {
    authenticationClient.configure(testProperties);
    authenticationClient.configure(testProperties);
  }

  @Test(expected = IllegalStateException.class)
  public void testSecondCallSetConnectionInfo() throws IOException {
    authenticationClient.setConnectionInfo("localhost", 443, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPropertiesConfigure() throws IOException {
    authenticationClient.configure(new Properties());
  }

  @Test
  public void testIsAuthEnabled() throws IOException {
    authenticationClient.configure(testProperties);
    assertTrue(authenticationClient.isAuthEnabled());
  }

  @Test(expected = IOException.class)
  public void testEmptyUrlListIsAuthEnabled() throws IOException {
    localTestServer.register("*", new EmptyUrlListHandler());
    authenticationClient.configure(testProperties);
    assertTrue(authenticationClient.isAuthEnabled());
  }

  @Test
  public void testAuthDisabledIsAuthEnabled() throws IOException {
    localTestServer.register("*", new AuthDisabledHandler());
    authenticationClient.configure(testProperties);
    assertFalse(authenticationClient.isAuthEnabled());
  }

  @After
  public void shutDown() throws Exception {
    localTestServer.stop();
  }
}
