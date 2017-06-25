package org.marid.jfx.beans;

import javafx.application.Platform;
import org.marid.concurrent.MaridTimerTask;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.logging.Level.SEVERE;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class OCleaner {

    public static boolean cleaningInProcess;

    private static final ConcurrentLinkedQueue<WeakReference<OCleanable>> CLEANABLES = new ConcurrentLinkedQueue<>();
    private static final Timer TIMER = new Timer(true);

    static {
        TIMER.schedule(new MaridTimerTask(t -> {
            final Queue<OCleanable> cleanables = new ConcurrentLinkedQueue<>();
            CLEANABLES.removeIf(ref -> {
                final OCleanable c = ref.get();
                if (c == null) {
                    return true;
                } else {
                    cleanables.add(c);
                    return false;
                }
            });
            Platform.runLater(() -> {
                cleaningInProcess = true;
                try {
                    cleanables.forEach(c -> {
                        try {
                            c.clean();
                        } catch (Exception x) {
                            log(SEVERE, "Unable to clean {0}", x, c);
                        }
                    });
                } finally {
                    cleaningInProcess = false;
                }
            });
        }), 60_000L, 60_000L);
    }

    public static void register(OCleanable cleanable) {
        CLEANABLES.add(new WeakReference<>(cleanable));
    }
}
