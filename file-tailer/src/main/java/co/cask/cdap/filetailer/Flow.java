
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

package co.cask.cdap.filetailer;

import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.tailer.LogTailer;

/**
 * Flow class contain sink and tailer for one flow
 */

public class Flow {
    private LogTailer logTailer;
    private FileTailerSink sink;

    public Flow(LogTailer tailer, FileTailerSink sink) {
        this.logTailer = tailer;
        this.sink = sink;
    }

    public void start() {
        logTailer.startWorker();
        sink.startWorker();
    }

    public void stop() {
        logTailer.stopWorker();
        sink.stopWorker();
    }
}
