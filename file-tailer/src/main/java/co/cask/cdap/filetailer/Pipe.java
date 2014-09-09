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

package co.cask.cdap.filetailer;

import co.cask.cdap.filetailer.metrics.FileTailerMetricsProcessor;
import co.cask.cdap.filetailer.sink.FileTailerSink;
import co.cask.cdap.filetailer.tailer.LogTailer;
import com.google.common.util.concurrent.AbstractIdleService;

/**
 * Contains the sink and tailer instances for one Pipe.
 */
public class Pipe extends AbstractIdleService {

    private final LogTailer logTailer;
    private final FileTailerSink sink;
    private final FileTailerMetricsProcessor metricsProcessor;

    public Pipe(LogTailer tailer, FileTailerSink sink, FileTailerMetricsProcessor metricsProcessor) {
        this.logTailer = tailer;
        this.sink = sink;
        this.metricsProcessor = metricsProcessor;
    }

    @Override
    public void startUp() {
        metricsProcessor.startAsync();
        logTailer.startAsync();
        sink.startAsync();
    }

    @Override
    public void shutDown() {
        metricsProcessor.stopAsync();
        logTailer.stopAsync();
        sink.stopAsync();
    }
}
