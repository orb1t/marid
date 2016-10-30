/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.preloader;

import javafx.scene.text.Text;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.marid.Ide;
import org.marid.concurrent.MaridTimerTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePreloaderLogHandler extends Handler {

    private final IdePreloader preloader;
    private final ConcurrentLinkedQueue<LogRecord> queue = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();

    IdePreloaderLogHandler(IdePreloader preloader) {
        this.preloader = preloader;
        setFormatter(new PatternFormatter("%m%n"));
        timer.schedule(new MaridTimerTask(task -> flush()), 100L, 30L);
    }

    @Override
    public void publish(LogRecord record) {
        queue.add(record);
    }

    @Override
    public void flush() {
        if (queue.isEmpty()) {
            return;
        }
        final ArrayList<Text> texts = new ArrayList<>();
        for (final Iterator<LogRecord> it = queue.iterator(); it.hasNext(); ) {
            final LogRecord record = it.next();
            try {
                final Text text = new Text(getFormatter().format(record));
                texts.add(text);
            } catch (RuntimeException x) {
                x.printStackTrace(); // it's wrong to log here
            } finally {
                it.remove();
            }
        }
        preloader.publishTexts(texts);
    }

    @Override
    public void close() {
        timer.cancel();
        flush();
        Ide.rootLogger.removeHandler(this);
    }
}
