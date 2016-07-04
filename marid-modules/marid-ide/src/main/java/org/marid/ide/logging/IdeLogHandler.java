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

package org.marid.ide.logging;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.marid.Ide;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogHandler extends Handler {

    private final ConcurrentLinkedQueue<LogRecord> queue = new ConcurrentLinkedQueue<>();
    private final ObservableList<LogRecord> logRecords = observableArrayList();

    public int getMaxLogRecords() {
        return Ide.PREFERENCES.getInt("maxLogRecords", 10_000);
    }

    public void setMaxLogRecords(int maxLogRecords) {
        Ide.PREFERENCES.putInt("maxLogRecords", maxLogRecords);
    }

    @Override
    public void publish(LogRecord record) {
        queue.add(record);
    }

    public ObservableList<LogRecord> getLogRecords() {
        return logRecords;
    }

    @Override
    @Scheduled(fixedDelay = 100L)
    public void flush() {
        if (!queue.isEmpty()) {
            final List<LogRecord> records = new ArrayList<>();
            for (final Iterator<LogRecord> it = queue.iterator(); it.hasNext(); ) {
                records.add(it.next());
                it.remove();
            }
            final int maxLogRecords = getMaxLogRecords();
            Platform.runLater(() -> {
                logRecords.addAll(records);
                final int size = logRecords.size();
                final int toRemove = size - maxLogRecords;
                if (toRemove > 0) {
                    logRecords.remove(0, toRemove);
                }
            });
        }
    }

    @Override
    public void close() {
    }
}
