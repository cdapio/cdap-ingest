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

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.flumesink.CdapSink;
import org.apache.flume.Channel;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.event.SimpleEvent;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 *
 */
public class CdapSinkTest extends CdapSink {
  @Test
  public void eventRollbackTest() throws EventDeliveryException {
    try {
      process();
    } catch (RuntimeException e) {
    return;
      }
  throw new RuntimeException();
  }

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
    Mockito.doThrow(new RuntimeException()).when(transaction).rollback();
    Mockito.when(channel.getTransaction()).thenReturn(transaction);
    Mockito.when(channel.take()).thenReturn(new SimpleEvent());
    return channel;
  }


  private void createStreamClient () throws NoSuchFieldException, IllegalAccessException {
    StreamWriter mockWriter = Mockito.mock(StreamWriter.class);
    Mockito.doThrow(new Exception()).when(mockWriter).write(
                org.mockito.Matchers.any(ByteBuffer.class), org.mockito.Matchers.anyMap());
   Field writerField = getClass().getDeclaredField("writer");
   writerField.setAccessible(true);
    StreamWriter writer = (StreamWriter) writerField.get(CdapSinkTest.this);
    writer = mockWriter;
  }


}
