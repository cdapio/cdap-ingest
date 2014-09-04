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
import co.cask.cdap.file.dropzone.polling.PollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Service to run multiple observers for polling dirs with some time interval
 */
public class DirPollingService implements Runnable, PollingService {
  private static final Logger LOG = LoggerFactory.getLogger(DirPollingService.class);

  private final long pollingInterval;
  private final Map<String, DirPollingObserver> observers = new ConcurrentHashMap<String, DirPollingObserver>();
  private Thread thread = null;
  private volatile boolean running = false;

  /**
   * Construct a monitor with a default pollingInterval of 10 seconds.
   */
  public DirPollingService() {
    this(10000);
  }

  /**
   * Construct a monitor with the specified pollingInterval.
   *
   * @param pollingInterval The amount of time in miliseconds to wait between checks of the file system
   */
  public DirPollingService(long pollingInterval) {
    this.pollingInterval = pollingInterval;
  }

  @Override
  public void startDirMonitor(File dir, PollingListener listener) {
    createDirs(dir.getAbsolutePath());
    DirPollingObserver observer = new DirPollingObserver(dir, listener);
    DirPollingObserver prevValue = observers.put(observer.getDirectory().getAbsolutePath(), observer);
    if (prevValue != null) {
      throw new IllegalArgumentException("Observer for folder {} already registered.");
    }
    LOG.info("Registered new Observer to the Polling Service: {}.", observer);
  }

  /**
   * Creates directory, and all parents directories, with specified path
   *
   * @param path the path to directory
   */
  private void createDirs(String path) {
    LOG.debug("Starting create directory with path: {}", path);
    File directory = new File(path);
    if (!directory.exists()) {
      boolean result = directory.mkdirs();
      LOG.debug("Creating directory result: {}", result);
    } else {
      LOG.debug("Directory/File with path: {} already exist", path);
    }
  }

  public void stopDirMonitor(File dir) {
    DirPollingObserver observer = observers.remove(dir.getAbsolutePath());
    if (observer == null) {
      throw new IllegalArgumentException("Observer is not found!");
    }
    LOG.info("Removed Observer from the Polling Service: {}.", observer);
  }

  @Override
  public synchronized void start() throws Exception {
    LOG.info("Try to start Directory Polling Service...");
    if (running) {
      throw new IllegalStateException("Monitor is already running");
    }
    running = true;
    thread = new Thread(this);
    thread.start();
    LOG.info("Successfully start of Directory Polling Service.");
  }

  @Override
  public synchronized void stop() throws Exception {
    LOG.info("Try to stop Directory Polling Service...");
    if (!running) {
      throw new IllegalStateException("Monitor is not running");
    }
    running = false;
    thread.interrupt();
    LOG.info("Successfully stop of Directory Polling Service.");
  }

  @Override
  public void removeFile(File folder, File file) {
    DirPollingObserver observer = observers.get(folder.getAbsolutePath());
    observer.removeProcessedFile(file);
  }

  /**
   * Run the Directory Polling Service.
   */
  @Override
  public void run() {
    LOG.debug("Polling process run.");
    while (running) {
      for (DirPollingObserver observer : observers.values()) {
        observer.checkAndNotify();
      }
      if (!running) {
        break;
      }
      try {
        Thread.sleep(pollingInterval);
      } catch (final InterruptedException ignored) {
      }
    }
  }

  /**
   *
   * @return the pollingInterval
   */
  public long getPollingInterval() {
    return pollingInterval;
  }
}
