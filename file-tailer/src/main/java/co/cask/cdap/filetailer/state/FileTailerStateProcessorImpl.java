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

import co.cask.cdap.filetailer.state.exception.FileTailerStateProcessorException;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * File Tailer state processor
 */
public class FileTailerStateProcessorImpl implements FileTailerStateProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FileTailerStateProcessorImpl.class);
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private static final Gson GSON = new Gson();

  private final File stateDir;
  private final File stateFile;

  public FileTailerStateProcessorImpl(File stateDir, String stateFileName) {
    this.stateDir = stateDir;
    stateFile = new File(stateDir, stateFileName);
  }

  @Override
  public void saveState(FileTailerState state) throws FileTailerStateProcessorException {
    try {
      Preconditions.checkNotNull(state);
    } catch (NullPointerException e) {
      LOG.info("Cannot save null state");
      return;
    }
    createDirs(stateDir);
    LOG.debug("Start saving File Tailer state ..");
    try {
      JsonWriter jsonWriter = new JsonWriter(Files.newWriter(stateFile, UTF_8));
      try {
        GSON.toJson(state, FileTailerState.class, jsonWriter);
        LOG.debug("File Tailer state saved successfully");
      } finally {
        try {
          jsonWriter.close();
        } catch (IOException e) {
          LOG.error("Cannot close JSON Writer for file {}: {}", stateFile.getAbsolutePath(), e.getMessage(), e);
        }
      }
    } catch (IOException e) {
      LOG.error("Cannot close JSON Writer for file {}: {}", stateFile.getAbsolutePath(), e.getMessage(), e);
    }
  }

  @Override
  public FileTailerState loadState() throws FileTailerStateProcessorException {
    if (!stateFile.exists()) {
      LOG.info("Not found state file: {}", stateFile.getAbsolutePath());
      return null;
    }
    LOG.debug("Start loading File Tailer state ..");
    try {
      BufferedReader reader = Files.newReader(stateFile, UTF_8);
      try {
        FileTailerState state = GSON.fromJson(reader, FileTailerState.class);
        LOG.debug("File Tailer state loaded successfully");
        return state;
      } finally {
        Closeables.closeQuietly(reader);
      }
    } catch (IOException e) {
      LOG.error("Can not load File Tailer state: {}", e);
      throw new FileTailerStateProcessorException(e.getMessage());
    }
  }

  /**
   * Creates all directories according to the {@link java.io.File directory}.
   *
   * @param directory the directory
   * @throws FileTailerStateProcessorException in case directories creating result where false
   */
  private void createDirs(File directory) throws FileTailerStateProcessorException {
    LOG.debug("Starting create directory with path: {}", directory.getAbsolutePath());
    if (!directory.exists()) {
      boolean result = directory.mkdirs();
      LOG.debug("Creating directory result: {}", result);
      if (!result) {
        throw new FileTailerStateProcessorException("Can not create File Tailer state directory");
      }
    } else {
      LOG.debug("Directory/File with path: {} already exist", directory.getAbsolutePath());
    }
  }
}
