/*
 * Copyright © 2015 Cask Data, Inc.
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

import co.cask.cdap.security.authentication.client.AuthenticationClient;

/**
 * Tests the rest stream writer for API v2.
 */
public class RestStreamWriterTestV2 extends RestStreamWriterTest {

  protected RestStreamClient buildClient(AuthenticationClient authClient) {
    return RestStreamClient.builder(testServerHost, testServerPort).version("v2").authClient(authClient).build();
  }
}
