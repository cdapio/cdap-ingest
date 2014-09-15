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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestClientUtilsTest {
  private HttpResponse response;

  @Before
  public void setUp() {
    response = mock(HttpResponse.class);
  }

  @Test
  public void testOkResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_BAD_REQUEST,
                                                "Bad Request");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = NotFoundException.class)
  public void testNotFoundResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND,
                                                "Not Found");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = NotAuthorizedException.class)
  public void testUnauthorizedResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_UNAUTHORIZED,
                                                "Unauthorized");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = ForbiddenException.class)
  public void testForbiddenResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_FORBIDDEN,
                                                "Forbidden");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = NotAllowedException.class)
  public void testNotAllowedResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_METHOD_NOT_ALLOWED,
                                                "Method Not Allowed");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = BadRequestException.class)
  public void testConflictResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_CONFLICT, "Conflict");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = InternalServerErrorException.class)
  public void testInternalServerErrorResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                                "Internal Server Error");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }

  @Test(expected = NotSupportedException.class)
  public void testNotImplementedResponseCodeAnalysis() {
    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_IMPLEMENTED,
                                                "Not Implemented");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClientUtils.verifyResponseCode(response);

    verify(response).getStatusLine();
  }
}
