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


package co.cask.cdap.flumesink;

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
 * Flume cdap sink
 */
public class CdapSink extends AbstractSink implements Configurable {

  private static final Logger logger = LoggerFactory.getLogger
    (CdapSink.class);

  private static final int DEFAULT_WRITER_POOL_SIZE = 10;

  private static final boolean DEFAULT_SSL = false;

  private static final String DEFAULT_VERSION = "v2";

  private static final String DEFAULT_STREAM_NAME = "stream1";

  private String host;
  private Integer port;
  private boolean sslEnabled;
  private int writerPoolSize;
  private String authToken;
  private String apiKey;
  private String version;
  private String streamName;
  private StreamWriter writer;

  @Override
  public void configure(Context context) {
    host = context.getString("host");
    port = context.getInteger("port");

    sslEnabled = context.getBoolean("ssl", DEFAULT_SSL);
    authToken = context.getString("authToken");
    apiKey = context.getString("apiKey");
    version = context.getString("version", DEFAULT_VERSION);
    writerPoolSize = context.getInteger("writerPoolSize", DEFAULT_WRITER_POOL_SIZE);
    streamName = context.getString("streamName", DEFAULT_STREAM_NAME);
    Preconditions.checkState(host != null, "No hostname specified");
    Preconditions.checkState(port != null, "No port specified");
  }

  @Override
  public Status process() throws EventDeliveryException {

    Status status = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();

    try {
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
        logger.error("Rpc Sink " + getName() + ": Unable to get event from" +
                       " channel " + channel.getName() + ". Exception follows.", t);
        status = Status.BACKOFF;
      } else {
        destroyClienConnection();
        throw new EventDeliveryException("Failed to send events", t);
      }
    } finally {
      transaction.close();
    }

    return status;
  }

  private void destroyClienConnection() {
    try {
      if (writer != null) {
        writer.close();
      }
    } catch (IOException e) {
      logger.error("error during closing writer ", e.getMessage());
    }
  }

  private void createStreamClient() throws URISyntaxException, IOException {

    RestStreamClient.Builder builder = RestStreamClient.builder(host, port);

    builder.ssl(sslEnabled);

    if (authToken != null && !authToken.equals("")) {
      builder.authToken(authToken);
    }

    if (apiKey != null && !apiKey.equals("")) {
      builder.apiKey(apiKey);
    }

    builder.writerPoolSize(writerPoolSize);

    builder.version(version);

    try {
      RestStreamClient client = builder.build();
      client.create(streamName);
      writer = client.createWriter(streamName);
    } catch (IOException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    } catch (URISyntaxException e) {
      throw new IOException("Can not create/get client stream by name:" + streamName + ": " + e.getMessage());
    }
  }

  public synchronized void start() {
    try {
      createStreamClient();
    } catch (Exception e) {
      logger.warn("Unable to create Stream client for host " + host
                    + ", port: " + port, e);
      destroyClienConnection();
    }
    super.start();
    logger.info("Cdap sink {} started.", getName());
  }

  public synchronized void stop() {
    destroyClienConnection();
    logger.info("Cdapsink {} stopping...", getName());
    super.stop();
  }
}

