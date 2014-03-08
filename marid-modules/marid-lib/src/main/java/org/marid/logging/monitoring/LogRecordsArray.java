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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogRecordsArray implements LogRecords, Serializable {

    private final LogRecord[] logRecords;

    public LogRecordsArray(Collection<LogRecord> logRecords) {
        this.logRecords = logRecords.toArray(new LogRecord[logRecords.size()]);
    }

    public LogRecordsArray(LogRecord... logRecords) {
        this.logRecords = logRecords;
    }

    @Override
    public Iterable<LogRecord> logRecords() {
        return Arrays.asList(logRecords);
    }
}
