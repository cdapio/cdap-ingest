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

package co.cask.cdap.filetailer.tailer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Filters the log files returned from the log directory.
 */
public class LogFilter implements FilenameFilter {

  private final Pattern logPattern;
  private final String baseLogFile;

  public LogFilter(String logFile, String logPattern) {
    this.logPattern = Pattern.compile(logPattern);
    this.baseLogFile = logFile;
  }

  @Override
  public boolean accept(File dir, String name) {
    return name.contains(baseLogFile) || logPattern.matcher(name).matches();
  }
}
