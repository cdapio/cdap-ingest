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

package co.cask.cdap.client.rest;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.exception.NotFoundException;
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;

import static org.junit.Assert.assertEquals;

public class RestStreamWriterTest extends RestTest {

  private StreamClient streamClient;
  private StreamWriter streamWriter;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    streamClient = new RestStreamClient.Builder(testServerHost, testServerPort).build();
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
      assertEquals(InternalServerErrorException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testBadRequestStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.BAD_REQUEST_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(BadRequestException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotFoundStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.NOT_FOUND_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(javax.ws.rs.NotFoundException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testConflictStringWrite() throws Exception {
    streamWriter = streamClient.createWriter(TestUtils.CONFLICT_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(BadRequestException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAuthorizedStringWrite() throws IOException, NotFoundException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(NotAuthorizedException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testSuccessAuthorizedStringWrite()
    throws IOException, NotFoundException, InterruptedException, ExecutionException {
    streamClient = new RestStreamClient.Builder(testServerHost, testServerPort).authToken(RestTest.AUTH_TOKEN).build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
  }

  @Test
  public void testNotAuthorizedEmptyTokenStringWrite() throws NotFoundException, IOException, InterruptedException {
    streamClient = new RestStreamClient.Builder(testServerHost, testServerPort).authToken(StringUtils.EMPTY).build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(NotAuthorizedException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAuthorizedUnknownTokenStringWrite() throws NotFoundException, IOException, InterruptedException {
    streamClient = new RestStreamClient.Builder(testServerHost, testServerPort).authToken("test").build();
    streamWriter = streamClient.createWriter(TestUtils.AUTH_STREAM_NAME + TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(NotAuthorizedException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testForbiddenStringWrite() throws NotFoundException, IOException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.FORBIDDEN_STREAM_NAME +
                                               TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(ForbiddenException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testNotAllowedStringWrite() throws NotFoundException, IOException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.NOT_ALLOWED_STREAM_NAME +
                                                TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);
    try {
      streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
    } catch (ExecutionException e) {
      assertEquals(NotAllowedException.class, e.getCause().getClass());
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

  @Test
  public void testFileSend() throws NotFoundException, IOException, InterruptedException, ExecutionException {
    streamWriter = streamClient.createWriter(TestUtils.FILE_STREAM_NAME +
                                                TestUtils.WRITER_TEST_STREAM_NAME_POSTFIX);

    File file = File.createTempFile("tmp", ".txt");
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(file));
      writer.write(RestTest.EXPECTED_WRITER_CONTENT);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
    streamWriter.send(file, MediaType.PLAIN_TEXT_UTF_8).get();
    file.delete();
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
