/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.file.dropzone.polling;

import co.cask.cdap.file.dropzone.config.FileDropZoneConfiguration;
import co.cask.cdap.file.dropzone.config.FileDropZoneConfigurationImpl;
import co.cask.cdap.file.dropzone.config.ObserverConfiguration;
import co.cask.cdap.file.dropzone.polling.dir.DirPollingService;
import co.cask.cdap.filetailer.config.Configuration;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.ConfigurationLoaderImpl;
import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;

import java.io.File;
import java.util.List;

/**
 * Creates and manage polling service
 */
public class PollingServiceManager {
  private final File confFile;
  private PollingService monitor;
  private FileDropZoneConfiguration configuration;

  public PollingServiceManager(File confFile) {
    this.confFile = confFile;
  }

  /**
   * Polling service manager setup
   *
   * @throws ConfigurationLoadingException if can not setup polling service manager
   */
  public void initManager() throws ConfigurationLoadingException {
    this.configuration = getConfiguration();
    this.monitor = new DirPollingService(configuration.getPollingInterval());
  }

  /**
   * Observers setup
   */
  public void initObservers() {
    List<ObserverConfiguration> observerConfList = configuration.getObserverConfiguration();
    for (ObserverConfiguration observerConf : observerConfList) {
      monitor.registerDirMonitor(observerConf.getPipeConf().getSourceConfiguration().getWorkDir(),
                                 new PollingListenerImpl(monitor, observerConf));
    }
  }

  /**
   * Return DropZone configuration
   *
   * @return DropZone configuration
   * @throws ConfigurationLoadingException if configuration load failed
   */
  private FileDropZoneConfiguration getConfiguration() throws ConfigurationLoadingException {
    ConfigurationLoader loader = new ConfigurationLoaderImpl();
    Configuration configuration = loader.load(confFile);
    return new FileDropZoneConfigurationImpl(configuration.getProperties());
  }

  /**
   * Start polling service
   */
  public void startMonitor() throws Exception {
    monitor.start();
  }

  /**
   * Stop polling service
   */
  public void stopMonitor() throws Exception {
    monitor.stop();
  }

}
