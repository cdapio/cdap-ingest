/*
 * Copyright © 2014-2015 Cask Data, Inc.
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
import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.common.http.exception.HttpFailureException;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link co.cask.cdap.client.rest.RestStreamClient} class.
 */
public abstract class RestStreamClientTest extends RestTest {
  private StreamClient streamClient;

  protected abstract RestStreamClient buildClient(AuthenticationClient authClient);

  private void createClient(AuthenticationClient authClient) throws IOException {
    if (streamClient != null) {
      streamClient.close();
    }
    streamClient = buildClient(authClient);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    createClient(null);
  }

  @Test
  public void testSuccessGetTTL() throws IOException {
    long ttl = streamClient.getTTL(TestUtils.SUCCESS_STREAM_NAME);
    assertTrue(ttl == STREAM_TTL);
  }

  @Test
  public void testNotFoundGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.NOT_FOUND_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  public void testBadRequestGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.BAD_REQUEST_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedEmptyTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testSuccessAuthGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    long ttl = streamClient.getTTL(TestUtils.SUCCESS_STREAM_NAME);
    assertTrue(ttl == STREAM_TTL);
  }

  @Test
  public void testForbiddenGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.FORBIDDEN_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getStatusCode());
    }
  }

  @Test
  public void testNotAcceptableGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.NOT_ALLOWED_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, e.getStatusCode());
    }
  }

  @Test
  public void testConflictGetTTL() throws IOException {
    try {
      streamClient.getTTL(TestUtils.CONFLICT_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, e.getStatusCode());
    }
  }

  @Test
  public void testServerErrorGetTTL() throws IOException {
    try {
      streamClient.getTTL(StringUtils.EMPTY);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getStatusCode());
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedGetTTL() throws IOException {
    streamClient.getTTL("Unknown");
  }

  @Test
  public void testSuccessSetTTL() throws IOException {
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test
  public void testNotFoundSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.NOT_FOUND_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  public void testBadRequestSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.BAD_REQUEST_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedEmptyTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testSuccessAuthSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test
  public void testForbiddenSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.FORBIDDEN_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getStatusCode());
    }
  }

  @Test
  public void testNotAcceptableSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.NOT_ALLOWED_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, e.getStatusCode());
    }
  }

  @Test
  public void testConflictSetTTL() throws IOException {
    try {
      streamClient.setTTL(TestUtils.CONFLICT_STREAM_NAME, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, e.getStatusCode());
    }
  }

  @Test
  public void testServerErrorSetTTL() throws IOException {
    try {
      streamClient.setTTL(StringUtils.EMPTY, STREAM_TTL);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getStatusCode());
    }
  }

  @Test
  public void testNotSupportedSetTTL() throws IOException {
    try {
      streamClient.setTTL("Unknown", STREAM_TTL);
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // PASS
    }
  }

  @Test
  public void testSuccessTruncate() throws IOException {
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testNotFoundTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.NOT_FOUND_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  public void testBadRequestTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.BAD_REQUEST_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedEmptyTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testSuccessAuthTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testForbiddenTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.FORBIDDEN_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getStatusCode());
    }
  }

  @Test
  public void testNotAcceptableTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.NOT_ALLOWED_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, e.getStatusCode());
    }
  }

  @Test
  public void testConflictTruncate() throws IOException {
    try {
      streamClient.truncate(TestUtils.CONFLICT_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, e.getStatusCode());
    }
  }

  @Test
  public void testServerErrorTruncate() throws IOException {
    try {
      streamClient.truncate(StringUtils.EMPTY);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getStatusCode());
    }
  }

  @Test
  public void testNotSupportedTruncate() throws IOException {
    try {
      streamClient.truncate("Unknown");
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // PASS
    }
  }

  @Test
  public void testSuccessCreate() throws IOException {
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testBadRequestCreate() throws IOException {
    try {
      streamClient.create(TestUtils.BAD_REQUEST_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedCreate() throws IOException {
    try {
      streamClient.create(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedEmptyTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.create(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    try {
      streamClient.create(TestUtils.AUTH_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
    }
  }

  @Test
  public void testSuccessAuthCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    createClient(authClient);
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testForbiddenCreate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.FORBIDDEN_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAcceptableCreate() throws IOException {
    try {
      streamClient.create(TestUtils.NOT_ALLOWED_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, e.getStatusCode());
    }
  }

  @Test
  public void testConflictCreate() throws IOException {
    try {
      streamClient.create(TestUtils.CONFLICT_STREAM_NAME);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, e.getStatusCode());
    }
  }

  @Test
  public void testServerErrorCreate() throws IOException {
    try {
      streamClient.create(StringUtils.EMPTY);
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getStatusCode());
    }
  }

  @Test
  public void testNotSupportedCreate() throws IOException {
    try {
      streamClient.create("Unknown");
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // PASS
    }
  }

  @Test
  public void testCreateWriter() throws IOException {
    StreamWriter streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME);
    assertNotNull(streamWriter);
    assertEquals(RestStreamWriter.class, streamWriter.getClass());
    RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
    assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
  }

  @Test
  public void testNotExistStreamCreateWriter() throws IOException {
    try {
      StreamWriter streamWriter = streamClient.createWriter(TestUtils.NOT_FOUND_STREAM_NAME);
      assertNotNull(streamWriter);
      assertEquals(RestStreamWriter.class, streamWriter.getClass());
      RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
      assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
      Assert.fail("Expected HttpFailureException");
    } catch (HttpFailureException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getStatusCode());
    }
  }

  @After
  public void shutDown() throws Exception {
    streamClient.close();
    super.shutDown();
  }
}
