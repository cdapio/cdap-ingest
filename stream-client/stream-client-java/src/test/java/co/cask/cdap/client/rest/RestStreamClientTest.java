/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.client.rest;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.common.http.exception.HttpFailureException;
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link co.cask.cdap.client.rest.RestStreamClient} class.
 */
public class RestStreamClientTest extends RestTest {
  private StreamClient streamClient;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).build();
  }

  @Test
  public void testSuccessGetTTL() throws IOException {
    long ttl = streamClient.getTTL(TestUtils.SUCCESS_STREAM_NAME);
    assertTrue(ttl == STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotFoundGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.NOT_FOUND_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testBadRequestGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedEmptyTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedUnknownTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    long ttl = streamClient.getTTL(TestUtils.SUCCESS_STREAM_NAME);
    assertTrue(ttl == STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testForbiddenGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAcceptableGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testConflictGetTTL() throws IOException {
    streamClient.getTTL(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testServerErrorGetTTL() throws IOException {
    streamClient.getTTL(StringUtils.EMPTY);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedGetTTL() throws IOException {
    streamClient.getTTL("Unknown");
  }

  @Test
  public void testSuccessSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotFoundSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.NOT_FOUND_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testBadRequestSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.BAD_REQUEST_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedEmptyTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedUnknownTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test
  public void testSuccessAuthSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testForbiddenSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.FORBIDDEN_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAcceptableSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.NOT_ALLOWED_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testConflictSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.CONFLICT_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = HttpFailureException.class)
  public void testServerErrorSetTTL() throws IOException {
    streamClient.setTTL(StringUtils.EMPTY, STREAM_TTL);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedSetTTL() throws IOException {
    streamClient.setTTL("Unknown", STREAM_TTL);
  }

  @Test
  public void testSuccessTruncate() throws IOException {
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotFoundTruncate() throws IOException {
    streamClient.truncate(TestUtils.NOT_FOUND_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testBadRequestTruncate() throws IOException {
    streamClient.truncate(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedTruncate() throws IOException {
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedEmptyTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedUnknownTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testForbiddenTruncate() throws IOException {
    streamClient.truncate(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAcceptableTruncate() throws IOException {
    streamClient.truncate(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testConflictTruncate() throws IOException {
    streamClient.truncate(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testServerErrorTruncate() throws IOException {
    streamClient.truncate(StringUtils.EMPTY);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedTruncate() throws IOException {
    streamClient.truncate("Unknown");
  }

  @Test
  public void testSuccessCreate() throws IOException {
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testBadRequestCreate() throws IOException {
    streamClient.create(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedCreate() throws IOException {
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedEmptyTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAuthorizedUnknownTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testForbiddenCreate() throws IOException {
    streamClient.create(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testNotAcceptableCreate() throws IOException {
    streamClient.create(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testConflictCreate() throws IOException {
    streamClient.create(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = HttpFailureException.class)
  public void testServerErrorCreate() throws IOException {
    streamClient.create(StringUtils.EMPTY);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedCreate() throws IOException {
    streamClient.create("Unknown");
  }

  @Test
  public void testCreateWriter() throws IOException {
    StreamWriter streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME);
    assertNotNull(streamWriter);
    assertEquals(RestStreamWriter.class, streamWriter.getClass());
    RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
    assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
  }

  @Test(expected = HttpFailureException.class)
  public void testNotExistStreamCreateWriter() throws IOException {
    StreamWriter streamWriter = streamClient.createWriter(TestUtils.NOT_FOUND_STREAM_NAME);
    assertNotNull(streamWriter);
    assertEquals(RestStreamWriter.class, streamWriter.getClass());
    RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
    assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
  }

  @After
  public void shutDown() throws Exception {
    streamClient.close();
    super.shutDown();
  }
}
