
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

package co.cask.cdap.filetailer.tailer;

import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.PipeConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 */
public class TailerLogUtils {

  public static void writeLineToFile(String filePath, String line) throws IOException {
    Writer writer = new FileWriter(filePath, true);
    writer.write(line + "\n");
    writer.flush();
    writer.close();
  }
  public static PipeConfiguration loadConfig() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Class<? extends Class> path1 =  TailerLogUtils.class.getClass();
    String path =  TailerLogUtils.class.getClassLoader().getResource("test4.properties").getFile();
    Configuration configuration = loader.load(path);
    List<PipeConfiguration> flowConfig = configuration.getPipesConfiguration();
    return flowConfig.get(0);
      }
  public static void createTestDirIfNeed() throws ConfigurationLoadingException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    File dirFile = new File(dir);
    if (!dirFile.exists()) {
      dirFile.mkdir();
    }
  }
  public static void clearTestDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.cleanDirectory(new File(dir));
  }
  public static void clearStateDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getDaemonDir();
    FileUtils.cleanDirectory(new File(dir));
  }

  public static void deleteTestDir() throws IOException {
    PipeConfiguration flowConf = loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.deleteDirectory(new File(dir));
  }
}
