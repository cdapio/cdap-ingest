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

package co.cask.cdap.filetailer.sink;

import co.cask.cdap.client.StreamWriter;
import co.cask.cdap.filetailer.event.FileTailerEvent;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerState;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dev on 18.08.14.
 */
public class FileTailerSinkTest {

  public static final int DEFAULT_QUEUE_SIZE = 10;
  public static final int TEST_EVENTS_SIZE = 20;
  public static final int CUSTOM_PACK_SIZE = 10;

  @Test
  public void basicTestWithDefaultPackSize() throws Exception {
    FileTailerStateProcessor stateProcessor = Mockito.mock(FileTailerStateProcessor.class);

    FileTailerQueue queue = new FileTailerQueue(DEFAULT_QUEUE_SIZE);

    StreamWriter writerMock = getDummyStreamWriter();
    FileTailerSink sink = new FileTailerSink(queue, writerMock,
                                             SinkStrategy.LOADBALANCE, stateProcessor);

    sink.startWorker();

    for (int i = 0; i < TEST_EVENTS_SIZE; i++) {
      queue.put(new FileTailerEvent(new FileTailerState("file", 0L, 42, 0L), "test", Charset.defaultCharset()));
    }

    Mockito.verify(writerMock, Mockito.timeout(10000).times(TEST_EVENTS_SIZE)).write("test", Charset.defaultCharset());

    sink.stopWorker();
  }

  @Test
  public void basicTestWithCustomPackSize() throws Exception {
    FileTailerStateProcessor stateProcessor = Mockito.mock(FileTailerStateProcessor.class);

    FileTailerQueue queue = new FileTailerQueue(DEFAULT_QUEUE_SIZE);

    StreamWriter writerMock = getDummyStreamWriter();
    FileTailerSink sink = new FileTailerSink(queue, writerMock,
                                             SinkStrategy.LOADBALANCE, stateProcessor, CUSTOM_PACK_SIZE);
    try {
      sink.startWorker();

      for (int i = 0; i < TEST_EVENTS_SIZE; i++) {
        queue.put(new FileTailerEvent(new FileTailerState("file", 0L, 42, 0L), "test", Charset.defaultCharset()));
      }

      Mockito.verify(writerMock,
                     Mockito.timeout(10000).times(TEST_EVENTS_SIZE)).write("test", Charset.defaultCharset());
    } finally {
      sink.stopWorker();
    }
  }

  @Test
  public void basicTestWithCustomPackSizeMultipleWriters() throws Exception {
    FileTailerStateProcessor stateProcessor = Mockito.mock(FileTailerStateProcessor.class);

    FileTailerQueue queue = new FileTailerQueue(DEFAULT_QUEUE_SIZE);

    final AtomicInteger count = new AtomicInteger(0);

    StreamWriter writers = getDummyConcurrentWriter(count);


    boolean success = false;

    FileTailerSink sink = new FileTailerSink(queue, writers, SinkStrategy.LOADBALANCE,
                                             stateProcessor, CUSTOM_PACK_SIZE);
    try {
      sink.startWorker();

      for (int i = 0; i < TEST_EVENTS_SIZE; i++) {
        queue.put(new FileTailerEvent(new FileTailerState("file", 0L, 42, 0L), "test", Charset.defaultCharset()));
      }

      int attempts = 10;
      while (attempts > 0) {
        attempts--;
        if (count.get() == TEST_EVENTS_SIZE) {
          success = true;
          break;
        }
        Thread.sleep(1000);
      }

    } finally {
      sink.stopWorker();
    }

    org.junit.Assert.assertTrue(success);
  }

  private StreamWriter getDummyConcurrentWriter(final AtomicInteger count) {
    StreamWriter writerMock = Mockito.mock(StreamWriter.class);

    Mockito.doAnswer(new Answer<ListenableFuture<Void>>() {
      @Override
      public ListenableFuture<Void> answer(InvocationOnMock invocationOnMock) throws Throwable {
        count.incrementAndGet();
        return Futures.immediateFuture((Void) null);
      }
    }).when(writerMock).write("test", Charset.defaultCharset());
    return writerMock;
  }

  private StreamWriter getDummyStreamWriter() {
    StreamWriter writerMock = Mockito.mock(StreamWriter.class);
    Mockito.when(writerMock.write("test", Charset.defaultCharset())).thenReturn(Futures.immediateFuture((Void) null));
    return writerMock;
  }

}
