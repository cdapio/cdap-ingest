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

package co.cask.cdap.filetailer.state;

import java.io.Serializable;

/**
 * File Tailer State presentation
 */
public class FileTailerState {

  private final String fileName;
  private final long position;
  private final int hash;
  private final long lastModifyTime;

  public FileTailerState(String fileName, long position, int hash, long lastModifyTime) {
    this.fileName = fileName;
    this.position = position;
    this.hash = hash;
    this.lastModifyTime = lastModifyTime;
  }

  /**
   * Retrieves the name of log file.
   *
   * @return the name of log file
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Retrieves the position in the log file.
   *
   * @return the position in log file
   */
  public long getPosition() {
    return position;
  }

  /**
   * Retrieves the hash of the last read line.
   *
   * @return the hash of last read line
   */
  public int getHash() {
    return hash;
  }

  /**
   * Retrieves the last modified time of the log file.
   *
   * @return the last modified time of log file
   */
  public long getLastModifyTime() {
    return lastModifyTime;
  }

  @Override
  public String toString() {
    return new StringBuffer("FileTailerState{")
      .append("fileName='").append(fileName).append('\'')
      .append(", position=").append(position)
      .append(", hash='").append(hash).append('\'')
      .append(", lastModifyTime=").append(lastModifyTime)
      .append('}').toString();
  }
}
