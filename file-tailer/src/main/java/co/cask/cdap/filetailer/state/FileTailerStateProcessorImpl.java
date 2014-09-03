/*
 * Copyright 2014 Cask Data, Inc.
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    createDirs(stateDirPath);
    LOG.debug("Start saving File Tailer state ..");
    JsonWriter jsonWriter = null;
    try {
      jsonWriter = new JsonWriter(new FileWriter(stateDirPath + "/" + stateFileName));
      jsonWriter.beginObject();
      jsonWriter.name("fileName");
      jsonWriter.value(state.getFileName());
      jsonWriter.name("position");
      jsonWriter.value(state.getPosition());
      jsonWriter.name("hash");
      jsonWriter.value(state.getHash());
      jsonWriter.name("lastModifyTime");
      jsonWriter.value(state.getLastModifyTime());
      jsonWriter.endObject();
      LOG.debug("File Tailer state saved successfully");
    } catch (IOException e) {
      LOG.error("Can not save File Tailer state: {}", e.getMessage());
      throw new FileTailerStateProcessorException(e.getMessage());
    } finally {
      try {
        if (jsonWriter != null) {
          jsonWriter.close();
        }
      } catch (IOException e) {
        LOG.error("Can not close JSON Writer for file {}: {}", stateDirPath + "/" + stateFileName, e.getMessage());
      }
    }
  }

  @Override
  public FileTailerState loadState() throws FileTailerStateProcessorException {
    if (!new File(stateDirPath + "/" + stateFileName).exists()) {
      LOG.info("Not found state file: {}", stateDirPath + "/" + stateFileName);
      return null;
    }
    FileTailerState state;
    LOG.debug("Start loading File Tailer state ..");
    JsonParser parser = new JsonParser();
    try {
      JsonObject jsonObject = (JsonObject) parser.parse(new FileReader(stateDirPath + "/" + stateFileName));
      String fileName = jsonObject.get("fileName").getAsString();
      long position = jsonObject.get("position").getAsLong();
      int hash = jsonObject.get("hash").getAsInt();
      long lastModifyTime = jsonObject.get("lastModifyTime").getAsLong();
      state = new FileTailerState(fileName, position, hash, lastModifyTime);
      LOG.debug("File Tailer state loaded successfully");
    } catch (IOException e) {
      LOG.error("Can not load File Tailer state: {}", e.getMessage());
      throw new FileTailerStateProcessorException(e.getMessage());
    }
    return state;
  }

  private void createDirs(String path) throws FileTailerStateProcessorException {
    LOG.debug("Starting create directory with path: {}", path);
    File directory = new File(path);
    if (!directory.exists()) {
      boolean result = directory.mkdirs();
      LOG.debug("Creating directory result: {}", result);
      if (!result) {
        throw new FileTailerStateProcessorException("Can not create File Tailer state directory");
      }
    } else {
      LOG.debug("Directory/File with path: {} already exist", path);
    }
  }
}
