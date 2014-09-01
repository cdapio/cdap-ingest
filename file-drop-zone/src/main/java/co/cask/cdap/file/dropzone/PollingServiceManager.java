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

package co.cask.cdap.file.dropzone;

import co.cask.cdap.file.dropzone.polling.PollingListenerImpl;
import co.cask.cdap.file.dropzone.polling.PollingService;
import co.cask.cdap.file.dropzone.polling.config.FileDropZoneConfiguration;
import co.cask.cdap.file.dropzone.polling.config.FileDropZoneConfigurationImpl;
import co.cask.cdap.file.dropzone.polling.config.ObserverConfiguration;
import co.cask.cdap.file.dropzone.polling.dir.DirPollingService;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * PollingServiceManager creates and manage polling service
 */
public class PollingServiceManager {
  private final String confPath;
  private PollingService monitor;
  private FileDropZoneConfiguration configuration;

  public PollingServiceManager(String confPath) {
    this.confPath = confPath;
  }

  public void initManager() throws ConfigurationLoadingException {
    this.configuration = getConfiguration();
    this.monitor = new DirPollingService(configuration.getPollingInterval());
  }

  /**
   * Observers setup
   *
   * @throws IOException if can not create client stream
   */
  public void initObservers() throws IOException {
    List<ObserverConfiguration> observerConfList = configuration.getObserverConfiguration();
    for (ObserverConfiguration observerConf : observerConfList) {
      monitor.startDirMonitor(new File(observerConf.getPipeConf().getSourceConfiguration().getWorkDir()),
                              new PollingListenerImpl(monitor, observerConf));
    }
  }

  private FileDropZoneConfiguration getConfiguration() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(confPath);
    return new FileDropZoneConfigurationImpl(configuration.getProperties());
  }

  public void startMonitor() throws Exception {
    monitor.start();
  }

  public void stopMonitor() throws Exception {
    monitor.stop();
  }

}
