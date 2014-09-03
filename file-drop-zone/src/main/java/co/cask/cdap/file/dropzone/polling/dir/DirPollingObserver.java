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

package co.cask.cdap.file.dropzone.polling.dir;

import co.cask.cdap.file.dropzone.polling.PollingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

/**
 * The Observer for check whether the file have been created and for remove already processed files
 */
public class DirPollingObserver {
  private static final Logger LOG = LoggerFactory.getLogger(DirPollingObserver.class);

  private static final File[] EMPTY_ARRAY = {};

  private final PollingListener listener;
  private final File rootFile;
  private final FileFilter fileFilter;
  private final Set<String> processedFiles;

  /**
   * Construct an observer for the specified directory.
   *
   * @param directory the directory to observe
   */
  public DirPollingObserver(File directory, PollingListener listener) {
    this(directory, listener, null);
  }

  /**
   * Construct an observer for the specified directory, file filter and file comparator.
   *
   * @param rootFile   the root directory to observe
   * @param fileFilter The file filter or null if none
   */
  protected DirPollingObserver(File rootFile, PollingListener listener, FileFilter fileFilter) {
    if (rootFile == null) {
      throw new IllegalArgumentException("Root directory is missing");
    }
    this.rootFile = rootFile;
    this.fileFilter = fileFilter;
    this.processedFiles = new HashSet<String>();
    this.listener = listener;
  }

  /**
   * Return the directory being observed.
   *
   * @return the directory being observed
   */
  public File getDirectory() {
    return rootFile;
  }

  /**
   * Check whether the file have been created.
   */
  public void checkAndNotify() {
    if (rootFile.exists()) {
      checkAndNotify(listFiles(rootFile));
    } else {
      listener.onException(new FileNotFoundException("Root directory: " + rootFile.getAbsolutePath() + " is missing"));
    }
  }

  /**
   * Check observed directory for files which have been created.
   *
   * @param files The current list of files
   */
  private void checkAndNotify(File[] files) {
    LOG.debug("Waiting for new log files {}", files);
    for (File file : files) {
      if (!file.isDirectory()) {
        checkFile(file);
      }
    }
  }

  /**
   * Fire file created events to the registered listeners.
   *
   * @param file The new file
   */
  private synchronized void checkFile(File file) {
    if (!processedFiles.contains(file.getAbsolutePath())) {
      processedFiles.add(file.getAbsolutePath());
      listener.onFileCreate(file);
    } else {
      LOG.info("File already processed {}.", file);
    }
  }

  /**
   * Remove file which was already successfully sent to gateway server, form polling dir and process set.
   *
   * @param file already processed file
   */
  public synchronized void removeProcessedFile(File file) {
    if (file.delete()) {
      processedFiles.remove(file.getAbsolutePath());
    } else {
      listener.onException(new IllegalArgumentException(
        String.format("Cannot remove specified file %s.", file.getAbsolutePath())));
    }
  }

  /**
   * List the contents of a directory.
   *
   * @param file The file to list the contents of
   * @return the directory contents or a zero length array if the empty or the file is not a directory
   */
  private File[] listFiles(File file) {
    File[] children = null;
    if (file.isDirectory()) {
      children = fileFilter == null ? file.listFiles() : file.listFiles(fileFilter);
    }
    if (children == null) {
      children = EMPTY_ARRAY;
    }
    return children;
  }

  /**
   * Provide a String representation of this observer.
   *
   * @return a String representation of this observer
   */
  @Override
  public String toString() {
    return "DirPollingObserver{" +
      "listener=" + listener +
      ", rootFile=" + rootFile +
      ", fileFilter=" + fileFilter +
      ", processedFiles=" + processedFiles +
      '}';
  }
}
