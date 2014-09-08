
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * File Tailer read data from log file tests
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
    RandomAccessFile reader = new RandomAccessFile(filePath, "r");
    Class params[] = {String.class, byte.class};
    Method method = tailer.getClass().getDeclaredMethod("tryReadLine", RandomAccessFile.class, byte.class);

    method.setAccessible(true);
    for (int i = 0; i < LINE_SIZE; i++) {
      String currLine = randomUtils.randomAlphanumeric(LINE_SIZE);
      lineList.add(currLine);
      TailerLogUtils.writeLineToFile(filePath, currLine);
    }
    for (String line: lineList) {
      Assert.assertEquals(line, method.invoke(tailer, reader, (byte) '\n'));
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
    RandomAccessFile reader = new RandomAccessFile(filePath, "r");
    Class params[] = {String.class, byte.class};
    Method method = tailer.getClass().getDeclaredMethod("tryReadLine", RandomAccessFile.class, byte.class);
    method.setAccessible(true);
    String str = (String) method.invoke(tailer, reader, (byte) '\n');
      Assert.assertEquals(0, str.length());
    }
  }







