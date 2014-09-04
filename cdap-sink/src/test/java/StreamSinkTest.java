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

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.client.rest.RestStreamWriter;
import co.cask.cdap.flume.StreamSink;
import org.apache.flume.Channel;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.event.SimpleEvent;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 *CDAP Sink Test.
 */
public class StreamSinkTest {
  @Test
  public void eventRollbackWhenWriterFailsTest() throws EventDeliveryException, IOException, URISyntaxException {
    MockStreamSink sink = new MockStreamSink();
    Field writerField = null;
    try {
      writerField = StreamSink.class.getDeclaredField("writer");
    } catch (NoSuchFieldException e) {
    }
    writerField.setAccessible(true);
    try {
      writerField.set(sink, getFailMockWriter());
    } catch (IllegalAccessException e) {
    }
    try {
      sink.process();
    } catch (EventDeliveryException e) {
      return;
    }
  }

  @Test
  public void eventSuccessfulProcessingTest() throws Exception {
    MockStreamSink sink = new MockStreamSink();
    Field writerField = null;
    try {
      writerField = StreamSink.class.getDeclaredField("writer");
    } catch (NoSuchFieldException e) {
    }
    writerField.setAccessible(true);
    try {
      writerField.set(sink, getPassMockWriter());
    } catch (IllegalAccessException e) {
    }
    try {
      sink.process();
    } catch (EventDeliveryException e) {
      throw new Exception("Error during event processing", e);
    }
  }


  public StreamWriter getFailMockWriter() {
    RestStreamWriter mockWriter = Mockito.mock(RestStreamWriter.class);
    Mockito.doThrow(new RuntimeException()).when(mockWriter).write(
      org.mockito.Matchers.any(ByteBuffer.class), org.mockito.Matchers.anyMap());
    return mockWriter;
  }

  public StreamWriter getPassMockWriter() {
    RestStreamWriter mockWriter = Mockito.mock(RestStreamWriter.class);
    Mockito.doReturn(null).when(mockWriter).write(
      org.mockito.Matchers.any(ByteBuffer.class), org.mockito.Matchers.anyMap());
    return mockWriter;
  }

}

class MockStreamSink extends StreamSink {
  @Override
  public synchronized Channel getChannel() {
    return getMockChannel();
  }

  private Channel getMockChannel() {
    Channel channel = Mockito.mock(Channel.class);
    Transaction transaction = Mockito.mock(Transaction.class);

    Mockito.doNothing().when(transaction).begin();
    Mockito.doNothing().when(transaction).commit();
    Mockito.doNothing().when(transaction).close();
    Mockito.doNothing().when(transaction).rollback();
    Mockito.when(channel.getTransaction()).thenReturn(transaction);
    Mockito.when(channel.take()).thenReturn(new SimpleEvent());
    return channel;
  }
}
