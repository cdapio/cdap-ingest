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
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link co.cask.cdap.client.rest.RestStreamWriter} class.
 */
public class RestStreamWriterTest extends RestTest {

  private StreamClient streamClient;
  private StreamWriter streamWriter;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).build();
  }

  @Test
  public void testSuccessStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
  }

  @Test
  public void testUnexpectedBodyStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write("Unexpected body", Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testBadRequestStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.BAD_REQUEST_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotFoundStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.NOT_FOUND_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testConflictStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.CONFLICT_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAuthorizedStringWrite() throws IOException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testSuccessAuthorizedStringWrite()
    throws IOException, InterruptedException, ExecutionException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(authClient.isAuthEnabled()).thenReturn(true);
    Mockito.when(accessToken.getValue()).thenReturn(RestTest.AUTH_TOKEN);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
  }

  @Test
  public void testNotAuthorizedEmptyTokenStringWrite() throws IOException, InterruptedException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn(StringUtils.EMPTY);
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenStringWrite() throws IOException, InterruptedException {
    AuthenticationClient authClient = Mockito.mock(AuthenticationClient.class);
    AccessToken accessToken = Mockito.mock(AccessToken.class);
    Mockito.when(authClient.getAccessToken()).thenReturn(accessToken);
    Mockito.when(accessToken.getValue()).thenReturn("test");
    Mockito.when(accessToken.getTokenType()).thenReturn("Bearer");
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authClient(authClient).build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testForbiddenStringWrite() throws IOException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.FORBIDDEN_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAllowedStringWrite() throws IOException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.NOT_ALLOWED_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(HttpFailureException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testSuccessByteBufferWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    streamWriter.write(ByteBuffer.wrap(RestTest.EXPECTED_WRITER_CONTENT.getBytes())).get();
  }

  @Test
  public void testSuccessByteBufferWithHeadersWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.WITH_CUSTOM_HEADER_STREAM_NAME
                                               + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    Map<String, String> headers = new HashMap<String, String>();
    headers.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    streamWriter.write(ByteBuffer.wrap(RestTest.EXPECTED_WRITER_CONTENT.getBytes()), headers).get();
  }

  @Test
  public void testSuccessStringWithHeadersWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.WITH_CUSTOM_HEADER_STREAM_NAME
                                               + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    Map<String, String> headers = new HashMap<String, String>();
    headers.put(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8, headers).get();
  }

  @After
  public void shutDown() throws Exception {
    if (streamWriter != null) {
      streamWriter.close();
    }
    streamClient.close();
    super.shutDown();
  }
}
