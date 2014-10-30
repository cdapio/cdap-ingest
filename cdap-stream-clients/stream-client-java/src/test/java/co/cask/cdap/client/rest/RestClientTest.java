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

import co.cask.cdap.common.http.exception.HttpFailureException;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RestClient} class.
 */
public class RestClientTest {

  private HttpResponse response;
  private HttpEntity httpEntity;

  @Before
  public void setUp() {
    response = mock(HttpResponse.class);
    httpEntity = mock(HttpEntity.class);
  }

  @Test
  public void testOkResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClient.responseCodeAnalysis(response);

    verify(response).getStatusLine();
  }

  @Test
  public void testBadRequestResponseCodeAnalysis() {
    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_BAD_REQUEST,
                                                "Bad Request");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_BAD_REQUEST, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testNotFoundResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND,
                                                "Not Found");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_NOT_FOUND, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testUnauthorizedResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_UNAUTHORIZED,
                                                "Unauthorized");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_UNAUTHORIZED, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testForbiddenResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_FORBIDDEN,
                                                "Forbidden");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_FORBIDDEN, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testNotAllowedResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_METHOD_NOT_ALLOWED,
                                                "Method Not Allowed");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_METHOD_NOT_ALLOWED, response);
    verify(response, new VerificationMode() {
      @Override
      public void verify(VerificationData verificationData) {
        Assert.assertEquals(2, verificationData.getAllInvocations().size());
      }
    }).getStatusLine();
  }

  @Test
  public void testConflictResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_CONFLICT, "Conflict");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_CONFLICT, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testInternalServerErrorResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                "Internal Server Error");
    when(response.getStatusLine()).thenReturn(statusLine);
    TestUtils.verifyResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, response);
    verify(response).getStatusLine();
  }

  @Test
  public void testNotImplementedResponseCodeAnalysis() {
    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_IMPLEMENTED,
                                                "Not Implemented");
    when(response.getStatusLine()).thenReturn(statusLine);
    try {
      RestClient.responseCodeAnalysis(response);
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) { }
    verify(response).getStatusLine();
  }

  @Test
  public void testGetEntityAsJsonObject() throws IOException {

    InputStream inputStream = new ByteArrayInputStream("{'test': 'Hello World'}".getBytes(Charsets.UTF_8));
    when(httpEntity.getContent()).thenReturn(inputStream);

    JsonObject jsonObject = RestClient.toJsonObject(httpEntity);

    assertEquals("Hello World", jsonObject.get("test").getAsString());
    verify(httpEntity, times(2)).getContent();
  }

  @Test(expected = IOException.class)
  public void testNullEntityGetEntityAsJsonObject() throws IOException {
    RestClient.toJsonObject(null);
  }

  @Test(expected = IOException.class)
  public void testNullContentGetEntityAsJsonObject() throws IOException {

    when(httpEntity.getContent()).thenReturn(null);

    RestClient.toJsonObject(httpEntity);

    verify(httpEntity).getContent();
  }

  @Test(expected = IOException.class)
  public void testEmptyContentGetEntityAsJsonObject() throws IOException {
    InputStream inputStream = new ByteArrayInputStream(StringUtils.EMPTY.getBytes(Charsets.UTF_8));
    when(httpEntity.getContent()).thenReturn(inputStream);

    RestClient.toJsonObject(httpEntity);

    verify(httpEntity).getContent();
  }
}
