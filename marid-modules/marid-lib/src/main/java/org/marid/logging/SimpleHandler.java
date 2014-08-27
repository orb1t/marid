/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleHandler extends Handler {

    private final BiConsumer<SimpleHandler, LogRecord> recordConsumer;
    private final Consumer<SimpleHandler> flusher;
    private final Consumer<SimpleHandler> closer;

    public SimpleHandler(BiConsumer<SimpleHandler, LogRecord> recordConsumer, Consumer<SimpleHandler> flusher, Consumer<SimpleHandler> closer) {
        this.recordConsumer = recordConsumer;
        this.flusher = flusher;
        this.closer = closer;
    }

    public SimpleHandler(BiConsumer<SimpleHandler, LogRecord> recordConsumer, Consumer<SimpleHandler> flusher) {
        this(recordConsumer, flusher, h -> {});
    }

    public SimpleHandler(BiConsumer<SimpleHandler, LogRecord> recordConsumer) {
        this(recordConsumer, h -> {}, h -> {});
    }

    @Override
    public void publish(LogRecord record) {
        recordConsumer.accept(this, record);
    }

    @Override
    public void close() throws SecurityException {
        closer.accept(this);
    }

    @Override
    public void flush() {
        flusher.accept(this);
    }
}
