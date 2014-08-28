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
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.IOException;
import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.assertEquals;


public class BasicRestAuthenticationClientTest extends RestAuthenticationClientTest {
  private AuthenticationClient authenticationClient;

  @Test
  public void testSuccessGetAccessToken() throws IOException {
    authenticationClient = new BasicRestAuthenticationClient(testServerHost, testServerPort,
                                                             AUTHENTICATED_USERNAME, AUTHENTICATED_PASSWORD);

    assertEquals(TOKEN, authenticationClient.getAccessToken());
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedGetAccessToken() throws IOException {
    authenticationClient = new BasicRestAuthenticationClient(testServerHost, testServerPort, "test", "test");
    authenticationClient.getAccessToken();
  }

  @Test(expected = IOException.class)
  public void testEmptyUsernameGetAccessToken() throws IOException {
    authenticationClient = new BasicRestAuthenticationClient(testServerHost, testServerPort, StringUtils.EMPTY, "test");
    authenticationClient.getAccessToken();
  }

  @Test(expected = IOException.class)
  public void testEmptyPassGetAccessToken() throws IOException {
    authenticationClient = new BasicRestAuthenticationClient(testServerHost, testServerPort, "test", StringUtils.EMPTY);
    authenticationClient.getAccessToken();
  }
}
