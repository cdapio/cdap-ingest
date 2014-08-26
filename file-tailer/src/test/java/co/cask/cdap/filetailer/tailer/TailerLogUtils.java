package co.cask.cdap.filetailer.tailer;

import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.FlowConfiguration;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TailerLogUtils {

  public static void writeLineToFile(String filePath,String line) throws IOException {
    Writer writer = new FileWriter(filePath,true);
    writer.write(line+"\n");
    writer.flush();
    writer.close();
  }
  public static FlowConfiguration loadConfig() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Class<? extends Class> path1 =  TailerLogUtils.class.getClass();
    String path =  TailerLogUtils.class.getClassLoader().getResource("test4.properties").getFile();
    Configuration configuration = loader.load(path);
    List<FlowConfiguration> flowConfig = configuration.getFlowsConfiguration();
    return flowConfig.get(0);
      }
  public static void createTestDirIfNeed() throws ConfigurationLoadingException {
    FlowConfiguration flowConf= loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    File dirFile = new File(dir);
    if (!dirFile.exists()) {
      dirFile.mkdir();
    }
  }
  public static void clearTestDir() throws IOException {
    FlowConfiguration flowConf= loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.cleanDirectory(new File(dir));
  }
  public static void deleteTestDir() throws IOException {
    FlowConfiguration flowConf= loadConfig();
    String dir = flowConf.getSourceConfiguration().getWorkDir();
    FileUtils.deleteDirectory(new File(dir));
  }
}
