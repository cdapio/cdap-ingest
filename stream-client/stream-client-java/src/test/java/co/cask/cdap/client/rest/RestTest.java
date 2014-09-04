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

package co.cask.cdap.client.rest;

import co.cask.cdap.client.rest.handlers.StreamConfigHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamInfoHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamTruncateHttpRequestHandler;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;

/**
 * Contains common fields for unit tests for the REST Stream Client API implementation.
 */
public class RestTest {
  public static final long STREAM_TTL = 86400;
  public static final String AUTH_TOKEN = "er4545556tfgbdsa9ddvgfgd9";
  public static final String EXPECTED_WRITER_CONTENT = "Hello World!";
  public static final String TEST_HEADER_NAME = "X-Continuuity-Test";
  public static final String TEST_HEADER_VALUE = "Test";

  private LocalTestServer localTestServer;

  protected String testServerHost;
  protected int testServerPort;

  private final HttpRequestHandler configHandler = new StreamConfigHttpRequestHandler();
  private final HttpRequestHandler infoHandler = new StreamInfoHttpRequestHandler();
  private final HttpRequestHandler truncateHandler = new StreamTruncateHttpRequestHandler();
  private final HttpRequestHandler streamsHandler = new StreamHttpRequestHandler();

  @Before
  public void setUp() throws Exception {
    localTestServer = new LocalTestServer(null, null);
    localTestServer.register("*/config", configHandler);
    localTestServer.register("*/truncate", truncateHandler);
    localTestServer.register("*/info", infoHandler);
    localTestServer.register("*", streamsHandler);
    localTestServer.start();
    testServerHost = localTestServer.getServiceAddress().getHostName();
    testServerPort = localTestServer.getServiceAddress().getPort();
  }

  @After
  public void shutDown() throws Exception {
    localTestServer.stop();
  }
}
