package org.marid.swing.log;

import org.marid.image.MaridIcons;
import org.marid.l10n.L10nSupport;
import org.marid.logging.AbstractHandler;
import org.marid.pref.PrefSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandler extends AbstractHandler implements PrefSupport, L10nSupport {

    private final int size;
    private final AtomicInteger counter = new AtomicInteger();
    private final ConcurrentLinkedQueue<LogRecord> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<LogComponent> components = new ConcurrentLinkedQueue<>();

    public SwingHandler() throws Exception {
        final String sizeText = LogManager.getLogManager().getProperty(getClass().getCanonicalName() + ".size");
        size = sizeText == null ? 65536 : Integer.parseInt(sizeText);
        if (getFormatter() == null) {
            setFormatter(new SwingHandlerFormatter());
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getSourceMethodName() != null) {
            return;
        }
        queue.add(record);
        counter.incrementAndGet();
        while (counter.get() > size) {
            if (queue.poll() != null) {
                counter.decrementAndGet();
            }
        }
        components.forEach(c -> c.publish(record));
    }

    public void show() {
        EventQueue.invokeLater(() -> new LogFrame().setVisible(true));
    }

    private class LogFrame extends JFrame {

        private final LogComponent logComponent;

        public LogFrame() {
            super(s("Marid log"));
            setAlwaysOnTop(getPref("alwaysOnTop", true));
            add(this.logComponent = new LogComponent(preferences(), queue, r -> true));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setIconImages(MaridIcons.ICONS);
            setPreferredSize(getPref("size", new Dimension(300, 400)));
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    components.add(logComponent);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    components.remove(logComponent);
                    if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                        putPref("size", getSize());
                    }
                }
            });
            pack();
            setLocationByPlatform(true);
        }
    }
}
