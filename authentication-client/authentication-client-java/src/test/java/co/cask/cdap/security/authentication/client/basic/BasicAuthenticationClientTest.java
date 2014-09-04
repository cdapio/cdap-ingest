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
import co.cask.cdap.security.authentication.client.basic.handlers.AuthenticationHandler;
import co.cask.cdap.security.authentication.client.basic.handlers.BaseHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.localserver.LocalTestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BasicAuthenticationClientTest {
  public static final String AUTHENTICATED_USERNAME = "admin";
  public static final String AUTHENTICATED_PASSWORD = "realtime";
  public static final String TOKEN = "SuccessGeneratedToken";
  public static final String TOKEN_TYPE = "Bearer";
  public static final String EMPTY_TOKEN_USERNAME = "emptyToken";
  public static final Long TOKEN_LIFE_TIME = 86400L;
  private static final String USERNAME_PROP_NAME = "security.auth.client.username";
  private static final String PASSWORD_PROP_NAME = "security.auth.client.password";
  private static final String HOSTNAME_PROP_NAME = "security.auth.client.gateway.hostname";
  private static final String PORT_PROP_NAME = "security.auth.client.gateway.port";

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
    testProperties.setProperty(HOSTNAME_PROP_NAME, testServerHost);
    testProperties.setProperty(PORT_PROP_NAME, String.valueOf(testServerPort));
    testProperties.setProperty(USERNAME_PROP_NAME, AUTHENTICATED_USERNAME);
    testProperties.setProperty(PASSWORD_PROP_NAME, AUTHENTICATED_PASSWORD);
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

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPropertiesConfigure() throws IOException {
    authenticationClient.configure(new Properties());
  }

  @Test
  public void testIsAuthEnabled() throws IOException {
    authenticationClient.configure(testProperties);
    assertTrue(authenticationClient.isAuthEnabled());
  }

  @After
  public void shutDown() throws Exception {
    localTestServer.stop();
  }
}
