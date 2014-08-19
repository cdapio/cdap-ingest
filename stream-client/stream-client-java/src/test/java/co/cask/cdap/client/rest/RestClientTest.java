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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestClientTest {

  private HttpResponse response;

  @Before
  public void setUp() {
    response = mock(HttpResponse.class);
  }

  @Test
  public void testOkResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClient.responseCodeAnalysis(response);

    verify(response).getStatusLine();
  }

  @Test(expected = BadRequestException.class)
  public void testBadRequestResponseCodeAnalysis() {

    StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_BAD_REQUEST,
                                                "Bad Request");
    when(response.getStatusLine()).thenReturn(statusLine);

    RestClient.responseCodeAnalysis(response);

    verify(response).getStatusLine();
  }
}
