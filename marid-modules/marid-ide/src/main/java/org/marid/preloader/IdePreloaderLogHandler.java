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
import org.marid.Ide;
import org.marid.concurrent.MaridTimerTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePreloaderLogHandler extends Handler {

    public static final IdePreloaderLogHandler IDE_PRELOADER_LOG_HANDLER = new IdePreloaderLogHandler();

    private final ConcurrentLinkedQueue<LogRecord> queue = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();

    private IdePreloaderLogHandler() {
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
        final StringBuffer buffer = new StringBuffer(128);
        final Locale locale = Locale.getDefault();
        for (final Iterator<LogRecord> it = queue.iterator(); it.hasNext(); ) {
            final LogRecord record = it.next();
            try {
                buffer.setLength(0);
                m(locale, record.getMessage(), buffer, record.getParameters());
                buffer.append(System.lineSeparator());
                final Text text = new Text(buffer.toString());
                texts.add(text);
            } catch (Exception x) {
                x.printStackTrace(); // it's wrong to log here
            } finally {
                it.remove();
            }
        }
        if (!texts.isEmpty()) {
            Ide.ide.notifyPreloader(new LogNotification(texts));
        }
    }

    @Override
    public void close() {
        timer.cancel();
        flush();
    }
}
