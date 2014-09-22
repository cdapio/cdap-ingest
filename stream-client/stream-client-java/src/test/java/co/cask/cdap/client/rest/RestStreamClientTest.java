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
import java.util.concurrent.Callable;

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

  @Test
  public void testNotFoundGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.NOT_FOUND_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testBadRequestGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.BAD_REQUEST_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedEmptyTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedUnknownTokenGetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
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

  @Test
  public void testForbiddenGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.FORBIDDEN_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAcceptableGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.NOT_ALLOWED_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testConflictGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(TestUtils.CONFLICT_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testServerErrorGetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.getTTL(StringUtils.EMPTY);
        return null;
      }
    });
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
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.NOT_FOUND_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testBadRequestSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.BAD_REQUEST_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedEmptyTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedUnknownTokenSetTTL() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
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

  @Test
  public void testForbiddenSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.FORBIDDEN_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testNotAcceptableSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.NOT_ALLOWED_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testConflictSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(TestUtils.CONFLICT_STREAM_NAME, STREAM_TTL);
        return null;
      }
    });
  }

  @Test
  public void testServerErrorSetTTL() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.setTTL(StringUtils.EMPTY, STREAM_TTL);
        return null;
      }
    });
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedSetTTL() throws IOException {
    streamClient.setTTL("Unknown", STREAM_TTL);
  }

  @Test
  public void testSuccessTruncate() throws IOException {
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testNotFoundTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.NOT_FOUND_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testBadRequestTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.BAD_REQUEST_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedEmptyTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedUnknownTokenTruncate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();

    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
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

  @Test
  public void testForbiddenTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.FORBIDDEN_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAcceptableTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.NOT_ALLOWED_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testConflictTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(TestUtils.CONFLICT_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testServerErrorTruncate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.truncate(StringUtils.EMPTY);
        return null;
      }
    });
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNotSupportedTruncate() throws IOException {
    streamClient.truncate("Unknown");
  }

  @Test
  public void testSuccessCreate() throws IOException {
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test
  public void testBadRequestCreate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.BAD_REQUEST_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedCreate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedEmptyTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testNotAuthorizedUnknownTokenCreate() throws IOException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();

    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.AUTH_STREAM_NAME);
        return null;
      }
    });
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
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.NOT_ALLOWED_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testConflictCreate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(TestUtils.CONFLICT_STREAM_NAME);
        return null;
      }
    });
  }

  @Test
  public void testServerErrorCreate() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        streamClient.create(StringUtils.EMPTY);
        return null;
      }
    });
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

  @Test
  public void testNotExistStreamCreateWriter() throws IOException {
    TestUtils.verifyException(HttpFailureException.class, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        StreamWriter streamWriter = streamClient.createWriter(TestUtils.NOT_FOUND_STREAM_NAME);
        assertNotNull(streamWriter);
        assertEquals(RestStreamWriter.class, streamWriter.getClass());
        RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
        assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
        return null;
      }
    });
  }

  @After
  public void shutDown() throws Exception {
    streamClient.close();
    super.shutDown();
  }
}
