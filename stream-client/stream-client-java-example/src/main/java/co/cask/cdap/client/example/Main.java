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

package co.cask.cdap.client.example;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamClient;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of using the StreamClient API.
 */
public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    final String streamName = "streamName";

    try {
      // Create StreamClient instance with mandatory fields 'host' and 'port'.
      // Optional configurations will be set as:
      // defaults: protocol : 'http', writerPoolSize: '10', version : 'v2'.
      StreamClient streamClient = RestStreamClient.builder("localhost", 10000).build();

      // Create StreamWriter Instance
      StreamWriter streamWriter = streamClient.createWriter(streamName);

      try {
        // Create Stream by id <streamName>
        streamClient.create(streamName);

        // Get current Stream TTL value by id <streamName>
        long currentTTL = streamClient.getTTL(streamName);
        LOG.info("Get TTL for {} stream: {}", currentTTL, streamName);
        long newTTL = 18000;

        // Update TTL value for Stream by id <streamName>
        streamClient.setTTL(streamName, newTTL);
        LOG.info("Set new TTL for {} stream: {}", newTTL, streamName);

        // Get current Stream TTL value by id <streamName> after updating for compare
        currentTTL = streamClient.getTTL(streamName);
        LOG.info("Was TTL updated successfully? {}", currentTTL == newTTL ? "YES" : "NO");


        String log = "192.0.2.0 - - [09/Apr/2012:08:40:43 -0400] \"GET /NoteBook/ HTTP/1.0\" 201 809 \"-\" " +
          "\"Example v0.0.0 (www.example.org)\"";

        // Upload stream event to server
        ListenableFuture<Void> future = streamWriter.write(log, null);
        LOG.info("Future {}", future.get());

        Futures.addCallback(future, new FutureCallback<Void>() {
          @Override
          public void onSuccess(Void contents) {
            LOG.info("Success write stream {}", streamName);
          }

          @Override
          public void onFailure(Throwable throwable) {
            LOG.error("Exception in stream write process", throwable);
          }
        });
      } finally {
        // Releasing all resources
        streamWriter.close();
        streamClient.close();
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
