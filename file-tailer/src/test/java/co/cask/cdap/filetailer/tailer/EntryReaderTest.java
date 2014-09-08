
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

package co.cask.cdap.filetailer.tailer;

import co.cask.cdap.filetailer.config.PipeConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EntryReaderTest {
  private static  final int LINE_SIZE = 10;

  @Before
  public void prepare() throws IOException {
    TailerLogUtils.createTestDirIfNeed();
    TailerLogUtils.clearTestDir();
  }

  @After
  public void clean() throws IOException {
TailerLogUtils.deleteTestDir();
  }

  @Test
  public void readLineTest() throws Exception {
    final  List<String>  lineList = new ArrayList<String>(LINE_SIZE);
    RandomStringUtils randomUtils = new RandomStringUtils();
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();
    String filePath = flowConfig.getSourceConfiguration().getWorkDir() + "/"
        + flowConfig.getSourceConfiguration().getFileName();

    LogTailer tailer = new LogTailer(flowConfig, null, null, null);
    File file = new File(filePath);
    file.createNewFile();
    FileChannel channel =  (new RandomAccessFile(filePath, "r")).getChannel();
    Class params[] = {String.class, byte.class};
    Method method = tailer.getClass().getDeclaredMethod("tryReadLine", FileChannel.class, char.class);
    method.setAccessible(true);
    Field field = tailer.getClass().getDeclaredField("charset");
    field.setAccessible(true);
    field.set(tailer, Charset.defaultCharset());
    for (int i = 0; i < LINE_SIZE; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      lineList.add(currLine);
      TailerLogUtils.writeLineToFile(filePath, currLine);
    }
    for (String line: lineList) {
      Assert.assertEquals(line, method.invoke(tailer, channel, '\n'));
    }
  }

  @Test
  public void readEmptyTest() throws Exception {
    PipeConfiguration flowConfig = TailerLogUtils.loadConfig();
    String filePath = flowConfig.getSourceConfiguration().getWorkDir() + "/"
      + flowConfig.getSourceConfiguration().getFileName();

    LogTailer tailer = new LogTailer(flowConfig, null, null, null);
    File file = new File(filePath);
    file.createNewFile();

    FileChannel channel =  (new RandomAccessFile(filePath, "r")).getChannel();
    Class params[] = {String.class, byte.class};
    Method method = tailer.getClass().getDeclaredMethod("tryReadLine", FileChannel.class, char.class);
    method.setAccessible(true);
    Field field = tailer.getClass().getDeclaredField("charset");
    field.setAccessible(true);
    field.set(tailer, Charset.defaultCharset());
    String str = (String) method.invoke(tailer, channel, '\n');
      Assert.assertEquals(0, str.length());
    }
  }







