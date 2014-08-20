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

package co.cask.cdap.filetailer.config;

import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigurationLoader interface implementation
 */
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoaderImpl.class);

  @Override
  public Configuration load(String path) throws ConfigurationLoadingException {
    LOG.debug("Start initializing loader with file: {}", path);
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(path));
      LOG.debug("Loader successfully initialized with file: {}", path);
    } catch (IOException e) {
      LOG.error("Can not load properties: {}", e.getMessage());
      throw new ConfigurationLoadingException(e.getMessage());
    }
    return new ConfigurationImpl(properties);
  }
}
