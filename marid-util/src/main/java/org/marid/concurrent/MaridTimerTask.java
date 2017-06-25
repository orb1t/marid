package org.marid.concurrent;

import javax.annotation.Nonnull;
import java.util.TimerTask;
import java.util.function.Consumer;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class MaridTimerTask extends TimerTask {

    private final Consumer<MaridTimerTask> task;

    public MaridTimerTask(@Nonnull Consumer<MaridTimerTask> task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.accept(this);
        } catch (RuntimeException x) {
            log(WARNING, "Timer task error", x);
        }
    }
}
