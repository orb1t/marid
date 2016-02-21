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

package org.marid.ide.panes.logging;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.pref.PrefSupport;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.prefs.PreferenceChangeListener;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogHandler extends Handler implements PrefSupport {

    private final ObservableList<LogRecord> logRecords = FXCollections.observableArrayList();
    private final AtomicInteger maxLogRecords = new AtomicInteger(getPref("maxLogRecords", 10_000));
    private final PreferenceChangeListener preferenceChangeListener = evt -> {
        switch (evt.getKey()) {
            case "maxLogRecords":
                maxLogRecords.set(Integer.parseInt(evt.getNewValue()));
                break;
        }
    };

    public IdeLogHandler() {
        preferences().addPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            if (Platform.isFxApplicationThread()) {
                add(record);
            } else {
                Platform.runLater(() -> add(record));
            }
        }
    }

    private void add(LogRecord logRecord) {
        logRecords.removeIf(r -> logRecords.size() >= maxLogRecords.get());
        logRecords.add(logRecord);
    }

    public ObservableList<LogRecord> getLogRecords() {
        return logRecords;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        preferences().removePreferenceChangeListener(preferenceChangeListener);
    }
}
