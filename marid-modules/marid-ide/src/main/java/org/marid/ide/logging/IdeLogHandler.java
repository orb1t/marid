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

import javafx.collections.ObservableList;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;
import static org.marid.IdePrefs.PREFERENCES;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogHandler extends Handler {

    public static final ObservableList<LogRecord> LOG_RECORDS = synchronizedObservableList(observableArrayList());

    private volatile int maxRecords;

    public IdeLogHandler() {
        maxRecords = PREFERENCES.getInt("maxLogRecords", 10_000);
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    @Override
    public void publish(LogRecord record) {
        LOG_RECORDS.add(record);
        while (true) {
            final int toDelete = LOG_RECORDS.size() - maxRecords;
            if (toDelete > 0) {
                try {
                    LOG_RECORDS.subList(0, toDelete).clear();
                    break;
                } catch (IndexOutOfBoundsException x) {
                    // ignore
                }
            } else {
                break;
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        PREFERENCES.putInt("maxLogRecords", maxRecords);
    }
}
