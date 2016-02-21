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

package org.marid.logging;

import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLogHandler extends Handler {

    private final Consumer<LogRecord> logRecordConsumer;
    private final Runnable onClose;
    private final Runnable onFlush;

    public MaridLogHandler(Consumer<LogRecord> logRecordConsumer, Runnable onClose, Runnable onFlush) {
        this.logRecordConsumer = logRecordConsumer;
        this.onClose = onClose;
        this.onFlush = onFlush;
    }

    public MaridLogHandler(Consumer<LogRecord> logRecordConsumer, Runnable onClose) {
        this(logRecordConsumer, onClose, () -> {});
    }

    public MaridLogHandler(Consumer<LogRecord> logRecordConsumer) {
        this(logRecordConsumer, () -> {});
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            logRecordConsumer.accept(record);
        }
    }

    @Override
    public void flush() {
        onFlush.run();
    }

    @Override
    public void close() {
        onClose.run();
    }
}
