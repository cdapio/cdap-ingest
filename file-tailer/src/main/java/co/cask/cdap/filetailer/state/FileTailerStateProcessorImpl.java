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

package co.cask.cdap.filetailer.state;

import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by root on 8/18/14.
 */
public class FileTailerStateProcessorImpl implements FileTailerStateProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerStateProcessorImpl.class);

  private String stateDirPath;

  private String stateFileName;

  public FileTailerStateProcessorImpl(String stateDirPath, String stateFileName) {
    this.stateDirPath = stateDirPath;
    this.stateFileName = stateFileName;
  }

  @Override
  public void saveState(FileTailerState state) throws FileTailerStateProcessorException {
    createDir(stateDirPath);
    LOG.debug("Start saving File Tailer state ..");
    try {
      FileOutputStream fileOut = new FileOutputStream(stateDirPath + "/" + stateFileName);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(state);
      out.close();
      fileOut.close();
      LOG.debug("File Tailer state saved successfully");
    } catch (IOException e) {
      LOG.error("Can not save File Tailer state: {}", e.getMessage());
      throw new FileTailerStateProcessorException(e.getMessage());
    }
  }

  @Override
  public FileTailerState loadState() throws FileTailerStateProcessorException {
    if (!new File(stateDirPath + "/" + stateFileName).exists()) {
      LOG.error("Not found state file: {}", stateDirPath + "/" + stateFileName);
      throw new FileTailerStateProcessorException("Not found state file: " + stateDirPath + "/" + stateFileName);
    }
    FileTailerState state;
    LOG.debug("Start loading File Tailer state ..");
    try {
      FileInputStream fileIn = new FileInputStream(stateDirPath + "/" + stateFileName);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      state = (FileTailerState) in.readObject();
      in.close();
      fileIn.close();
      LOG.debug("File Tailer state loaded successfully");
    } catch (IOException e) {
      LOG.error("Can not load File Tailer state: {}", e.getMessage());
      throw new FileTailerStateProcessorException(e.getMessage());
    } catch (ClassNotFoundException e) {
      LOG.error("Can not found class for File Tailer state: {}", e.getMessage());
      throw new FileTailerStateProcessorException(e.getMessage());
    }
    return state;
  }

  private void createDir(String path) throws FileTailerStateProcessorException {
    LOG.debug("Starting create directory with path: {}", path);
    File directory = new File(path);
    if (!directory.exists()) {
      boolean result = directory.mkdir();
      LOG.debug("Creating directory result: {}", result);
      if (!result) {
        throw new FileTailerStateProcessorException("Can not create File Tailer state directory");
      }
    } else {
      LOG.debug("Directory/File with path: {} already exist", path);
    }
  }
}
