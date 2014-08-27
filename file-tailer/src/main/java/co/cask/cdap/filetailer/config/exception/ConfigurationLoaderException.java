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

package co.cask.cdap.filetailer.config.exception;

/**
 * ConfigurationLoadingException is exception that throws when ConfigurationLoader error occurred.
 * For example, configuration file does not exists.
 */
public class ConfigurationLoaderException extends RuntimeException {

  /**
   * Constructs an new ConfigurationLoadingException with the specified detail message.
   *
   * @param message the detail message
   */
  public ConfigurationLoaderException(String message) {
    super(message);
  }

}
