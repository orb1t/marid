/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeMavenLogHandler extends Handler {

    public final ObservableList<LogRecord> records = FXCollections.observableArrayList();

    public IdeMavenLogHandler(int threadId) {
        setFilter(r -> r.getThreadID() == threadId);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            Platform.runLater(() -> records.add(record));
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
