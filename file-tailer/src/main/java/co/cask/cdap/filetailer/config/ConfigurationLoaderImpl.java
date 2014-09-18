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

package co.cask.cdap.filetailer.config;

import co.cask.cdap.filetailer.config.exception.ConfigurationLoadingException;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigurationLoader interface implementation
 */
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoaderImpl.class);

  @Override
  public Configuration load(File file) throws ConfigurationLoadingException {
    LOG.debug("Start initializing loader with file: {}", file.getAbsolutePath());
    Properties properties = new Properties();
    try {
      InputStream is = new FileInputStream(file);
      try {
        properties.load(is);
        LOG.debug("Loader successfully initialized with file: {}", file.getAbsolutePath());
      } finally {
        Closeables.closeQuietly(is);
      }
    } catch (IOException e) {
      LOG.error("Cannot load properties", e);
      throw new ConfigurationLoadingException("Cannot load properties: " + e.getMessage());
    }
    return new ConfigurationImpl(properties);
  }
}
