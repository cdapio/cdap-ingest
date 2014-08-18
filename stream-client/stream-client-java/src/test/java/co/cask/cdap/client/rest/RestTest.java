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

import co.cask.cdap.client.rest.handlers.StreamConfigHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamInfoHttpRequestHandler;
import co.cask.cdap.client.rest.handlers.StreamTruncateHttpRequestHandler;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;

/**
 * @author Alina Makogon amakogon@cybervisiontech.com
 *         Date: 8/18/14
 */
public class RestTest {
  public static final long STREAM_TTL = 86400;
  public static final String AUTH_TOKEN = "er4545556tfgbdsa9ddvgfgd9";

  protected LocalTestServer localTestServer;
  protected String testServerHost;
  protected int testServerPort;

  private final HttpRequestHandler configHandler = new StreamConfigHttpRequestHandler();
  private final HttpRequestHandler infoHandler = new StreamInfoHttpRequestHandler();
  private final HttpRequestHandler truncateHandler = new StreamTruncateHttpRequestHandler();
  private final HttpRequestHandler streamsHandler = new StreamHttpRequestHandler();

  @Before
  public void setUp() throws Exception {
    localTestServer = new LocalTestServer(null, null);
//    localTestServer.register("*/streams/*", streamsHandler);
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
