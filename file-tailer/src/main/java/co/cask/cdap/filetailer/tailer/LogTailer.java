package co.cask.cdap.filetailer.tailer;

import co.cask.cdap.filetailer.AbstractWorker;
import co.cask.cdap.filetailer.config.ConfigurationLoader;
import co.cask.cdap.filetailer.config.FlowConfiguration;
import co.cask.cdap.filetailer.queue.FileTailerQueue;
import co.cask.cdap.filetailer.state.FileTailerStateProcessor;

/**
 * Created by yura on 20.08.14.
 */
public abstract class LogTailer extends AbstractWorker {
    @Override
    public abstract void run();

}
