
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * AbstractWorker class implement start/stop method
 */
public class AbstractWorker implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractWorker.class);
    private Thread worker;
    @Override
    public void run() {

    }

    public void startWorker() {
        if (worker == null) {
            worker = new Thread(this);
            worker.start();
        } else {
            LOG.warn("{} deamon is already started!", this.getClass().getName());
            throw new IllegalStateException(this.getClass().getName() + "  deamon is already started!");
        }
    }

    public void stopWorker() {
        if (worker != null) {
            worker.interrupt();
        } else {
            LOG.warn("{} deamon was not started!", this.getClass().getName());
            throw new IllegalStateException(this.getClass().getName() + "  deamon is already started!");

        }
    }
}
