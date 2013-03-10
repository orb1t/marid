/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.logging;

import java.util.Arrays;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.LogRecord;

/**
 * Memory handler.
 *
 * @author Dmitry Ovchinnikov
 */
public class MemoryHandler extends AbstractHandler {

    private LogRecord[] records = new LogRecord[0];
    private int maxRecords;
    private final LinkedList<MemoryHandlerListener> listeners = new LinkedList<>();

    public MemoryHandler() throws Exception {
        String maxRecordsText = manager.getProperty("maxRecords");
        maxRecords = maxRecordsText != null ? Integer.parseInt(maxRecordsText) : 1024;
    }

    public synchronized void addListener(MemoryHandlerListener listener) {
        listeners.add(listener);
        listener.recordsSet(records);
    }

    public synchronized void removeListener(MemoryHandlerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        int n = records.length;
        if (n < maxRecords) {
            records = Arrays.copyOf(records, n + 1);
            records[n] = record;
            for (Iterator<MemoryHandlerListener> i = listeners.descendingIterator(); i.hasNext();) {
                i.next().recordAdded(record);
            }
        } else {
            System.arraycopy(records, 1, records, 0, n - 1);
            records[n - 1] = record;
            for (Iterator<MemoryHandlerListener> i = listeners.descendingIterator(); i.hasNext();) {
                i.next().recordInserted(record);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public synchronized void close() throws SecurityException {
        listeners.clear();
    }

    public static interface MemoryHandlerListener extends EventListener {

        public void recordsSet(LogRecord... records);

        public void recordAdded(LogRecord record);

        public void recordInserted(LogRecord record);
    }
}
