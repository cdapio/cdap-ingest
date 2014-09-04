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


package co.cask.cdap.flume;

import co.cask.cdap.client.StreamClient;
import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamClient;
import com.google.common.base.Preconditions;
import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * CDAP Sink, a Flume sink implementation.
 */
public class StreamSink extends AbstractSink implements Configurable {

  private static final Logger LOG = LoggerFactory.getLogger
    (StreamSink.class);

  private static final int DEFAULT_WRITER_POOL_SIZE = 10;

  private static final boolean DEFAULT_SSL = false;

  private static final String DEFAULT_VERSION = "v2";

  private static final int DEFAULT_PORT = 10000;

  private String host;
  private Integer port;
  private boolean sslEnabled;
  private int writerPoolSize;
  private String authToken;
  private String version;
  private String streamName;
  private StreamWriter writer;

  @Override
  public void configure(Context context) {
    host = context.getString("host");
    port = context.getInteger("port", DEFAULT_PORT);
    sslEnabled = context.getBoolean("sslEnabled", DEFAULT_SSL);
    authToken = context.getString("authToken");
    version = context.getString("version", DEFAULT_VERSION);
    writerPoolSize = context.getInteger("writerPoolSize", DEFAULT_WRITER_POOL_SIZE);
    streamName = context.getString("streamName");
    Preconditions.checkState(host != null, "No hostname specified");
    Preconditions.checkState(streamName != null, "No stream name specified");
  }

  @Override
  public Status process() throws EventDeliveryException {

    Status status = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();

    try {
      tryReopenClientConnection();
      transaction.begin();
      Event event = channel.take();
      if (event != null) {
        writer.write(ByteBuffer.wrap(event.getBody()), event.getHeaders());
      }
      transaction.commit();

    } catch (Throwable t) {
      transaction.rollback();
      if (t instanceof Error) {
        throw (Error) t;
      } else if (t instanceof ChannelException) {
        LOG.error("RPC Sink {}: Unable to get event from channel {} ", getName(), channel.getName());
        status = Status.BACKOFF;
      } else {
        destroyClientConnection();
        throw new EventDeliveryException("Failed to send events", t);
      }
    } finally {
      transaction.close();
    }

    return status;
  }

  private void destroyClientConnection() {
    try {
      if (writer != null) {
        writer.close();
      }
    } catch (IOException e) {
      LOG.error("Error closing writer: {}", e.getMessage());
    }
    writer = null;
  }

  private void tryReopenClientConnection() throws URISyntaxException, IOException {
    if (writer == null) {
      LOG.debug("Trying to reopen stream writer ");
      createStreamClient();
    }
  }

  private void createStreamClient() throws URISyntaxException, IOException {

    RestStreamClient.Builder builder = RestStreamClient.builder(host, port);

    builder.ssl(sslEnabled);

    if (authToken != null && !authToken.equals("")) {
      builder.authToken(authToken);
    }

    builder.writerPoolSize(writerPoolSize);

    builder.version(version);

    try {
      StreamClient client = builder.build();
      client.create(streamName);
      writer = client.createWriter(streamName);
    } catch (IOException e) {
      throw new IOException("Can not create/get client stream by name: " + streamName + ": " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new IOException("Can not create/get client stream by name: " + streamName + ": " + e.getMessage());
    }
  }

  public synchronized void start() {
    super.start();
    try {
      createStreamClient();
    } catch (Exception e) {
      LOG.warn("Unable to create Stream client for host: {}, port: {}", host, port);
      destroyClientConnection();
    }
    LOG.info("Cdap sink {} started.", getName());
  }

  public synchronized void stop() {
    destroyClientConnection();
    LOG.info("Cdapsink {} stopping...", getName());
    super.stop();
  }
}

