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
import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RestStreamWriterTest extends RestTest {

  private StreamClient streamClient;
  private StreamWriter streamWriter;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    streamClient = new RestStreamClient.Builder(testServerHost, testServerPort).build();
  }

  @Test
  public void testSuccessStringWrite() throws IOException, ExecutionException, InterruptedException {
    streamWriter = streamClient.createWriter(TestUtils.SUCCESS_STREAM_NAME);
    streamWriter.write(RestTest.EXPECTED_WRITER_CONTENT, Charsets.UTF_8).get();
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
