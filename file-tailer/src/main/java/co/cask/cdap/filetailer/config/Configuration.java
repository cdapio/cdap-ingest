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

import java.util.List;

/**
 * Configuration s design for getting all properties from configuration file
 */
public interface Configuration {

  /**
   * Returns configurations of each pipe
   *
   * @return configurations of each pipe
   */
  List<PipeConfiguration> getPipesConfiguration();

  /**
   * Returns polling interval
   *
   * @return polling interval
   */
  long getPollingInterval();

  /**
   * Returns configurations of each observer
   *
   * @return configurations of each observer
   */
  public List<PipeConfiguration> getObserverConfiguration();
}
