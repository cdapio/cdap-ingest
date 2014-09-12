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

  public String getFileName() {
    return fileName;
  }

  public long getPosition() {
    return position;
  }

  public int getHash() {
    return hash;
  }

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
