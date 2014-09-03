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
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;

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

  @Test(expected = NotFoundException.class)
  public void testNotFoundGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.NOT_FOUND_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedEmptyTokenGetTTL() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(StringUtils.EMPTY).build();
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedUnknownTokenGetTTL() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken("test").build();
    streamClient.getTTL(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthGetTTL() throws NotFoundException, IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(AUTH_TOKEN).build();
    long ttl = streamClient.getTTL(TestUtils.SUCCESS_STREAM_NAME);
    assertTrue(ttl == STREAM_TTL);
  }

  @Test(expected = ForbiddenException.class)
  public void testForbiddenGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = NotAllowedException.class)
  public void testNotAcceptableGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testConflictGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = InternalServerErrorException.class)
  public void testServerErrorGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL(StringUtils.EMPTY);
  }

  @Test(expected = NotSupportedException.class)
  public void testNotSupportedGetTTL() throws IOException, NotFoundException {
    streamClient.getTTL("Unknown");
  }

  @Test
  public void testSuccessSetTTL() throws NotFoundException, IOException {
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = NotFoundException.class)
  public void testNotFoundSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.NOT_FOUND_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.BAD_REQUEST_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedEmptyTokenSetTTL() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(StringUtils.EMPTY).build();
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedUnknownTokenSetTTL() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken("test").build();
    streamClient.setTTL(TestUtils.AUTH_STREAM_NAME, STREAM_TTL);
  }

  @Test
  public void testSuccessAuthSetTTL() throws NotFoundException, IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(AUTH_TOKEN).build();
    streamClient.setTTL(TestUtils.SUCCESS_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = ForbiddenException.class)
  public void testForbiddenSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.FORBIDDEN_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = NotAllowedException.class)
  public void testNotAcceptableSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.NOT_ALLOWED_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = BadRequestException.class)
  public void testConflictSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(TestUtils.CONFLICT_STREAM_NAME, STREAM_TTL);
  }

  @Test(expected = InternalServerErrorException.class)
  public void testServerErrorSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL(StringUtils.EMPTY, STREAM_TTL);
  }

  @Test(expected = NotSupportedException.class)
  public void testNotSupportedSetTTL() throws IOException, NotFoundException {
    streamClient.setTTL("Unknown", STREAM_TTL);
  }

  @Test
  public void testSuccessTruncate() throws NotFoundException, IOException {
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = NotFoundException.class)
  public void testNotFoundTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.NOT_FOUND_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedEmptyTokenTruncate() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(StringUtils.EMPTY).build();
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedUnknownTokenTruncate() throws IOException, NotFoundException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken("test").build();
    streamClient.truncate(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthTruncate() throws NotFoundException, IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(AUTH_TOKEN).build();
    streamClient.truncate(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = ForbiddenException.class)
  public void testForbiddenTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = NotAllowedException.class)
  public void testNotAcceptableTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testConflictTruncate() throws IOException, NotFoundException {
    streamClient.truncate(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = InternalServerErrorException.class)
  public void testServerErrorTruncate() throws IOException, NotFoundException {
    streamClient.truncate(StringUtils.EMPTY);
  }

  @Test(expected = NotSupportedException.class)
  public void testNotSupportedTruncate() throws IOException, NotFoundException {
    streamClient.truncate("Unknown");
  }

  @Test
  public void testSuccessCreate() throws IOException {
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestCreate() throws IOException {
    streamClient.create(TestUtils.BAD_REQUEST_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedCreate() throws IOException {
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedEmptyTokenCreate() throws IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(StringUtils.EMPTY).build();
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test(expected = NotAuthorizedException.class)
  public void testNotAuthorizedUnknownTokenCreate() throws IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken("test").build();
    streamClient.create(TestUtils.AUTH_STREAM_NAME);
  }

  @Test
  public void testSuccessAuthCreate() throws IOException {
    streamClient = RestStreamClient.builder(testServerHost, testServerPort).authToken(AUTH_TOKEN).build();
    streamClient.create(TestUtils.SUCCESS_STREAM_NAME);
  }

  @Test(expected = ForbiddenException.class)
  public void testForbiddenCreate() throws IOException {
    streamClient.create(TestUtils.FORBIDDEN_STREAM_NAME);
  }

  @Test(expected = NotAllowedException.class)
  public void testNotAcceptableCreate() throws IOException {
    streamClient.create(TestUtils.NOT_ALLOWED_STREAM_NAME);
  }

  @Test(expected = BadRequestException.class)
  public void testConflictCreate() throws IOException {
    streamClient.create(TestUtils.CONFLICT_STREAM_NAME);
  }

  @Test(expected = InternalServerErrorException.class)
  public void testServerErrorCreate() throws IOException {
    streamClient.create(StringUtils.EMPTY);
  }

  @Test(expected = NotSupportedException.class)
  public void testNotSupportedCreate() throws IOException {
    streamClient.create("Unknown");
  }

  @Test
  public void testCreateWriter() throws NotFoundException, IOException {
    StreamWriter streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME);
    assertNotNull(streamWriter);
    assertEquals(RestStreamWriter.class, streamWriter.getClass());
    RestStreamWriter restStreamWriter = (RestStreamWriter) streamWriter;
    assertEquals(TestUtils.SUCCESS_STREAM_NAME, restStreamWriter.getStreamName());
  }

  @Test(expected = NotFoundException.class)
  public void testNotExistStreamCreateWriter() throws NotFoundException, IOException {
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
