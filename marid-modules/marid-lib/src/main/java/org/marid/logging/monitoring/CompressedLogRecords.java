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

package org.marid.logging.monitoring;

import org.marid.io.GzipInputStream;
import org.marid.io.GzipOutputStream;
import org.marid.logging.monitoring.LogRecords;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class CompressedLogRecords implements LogRecords, Serializable {

    private final byte[] data;

    public CompressedLogRecords(byte[] data) {
        this.data = data;
    }

    public CompressedLogRecords(Iterable<LogRecord> logRecords) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(new GzipOutputStream(bos, 1024, false))) {
            for (final LogRecord logRecord : logRecords) {
                oos.writeObject(logRecord);
            }
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
        data = bos.toByteArray();
    }

    public CompressedLogRecords(LogRecord... logRecords) {
        this(Arrays.asList(logRecords));
    }

    @Override
    public Iterable<LogRecord> logRecords() {
        final ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try (final ObjectInputStream ois = new ObjectInputStream(new GzipInputStream(bis, 1024))) {
            return () -> new Iterator<LogRecord>() {
                @Override
                public boolean hasNext() {
                    return bis.available() > 0;
                }

                @Override
                public LogRecord next() {
                    try {
                        return (LogRecord) ois.readObject();
                    } catch (IOException | ClassNotFoundException x) {
                        throw new IllegalStateException(x);
                    }
                }
            };
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
}
