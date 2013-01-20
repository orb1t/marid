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
package org.marid.db.storage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.naming.Name;

/**
 * Data logging storage.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface LoggingStorage extends HistoricalStorage {

    /**
     * Logs log records.
     * @param logRecords Log records.
     * @throws IOException An I/O exception.
     */
    void log(LogRecord... logRecords) throws IOException;

    /**
     * Logs log records.
     * @param logRecords Log record list.
     * @throws IOException An I/O exception.
     */
    void log(List<LogRecord> logRecords) throws IOException;

    /**
     * Queries all the log records with the given level and log name.
     * @param name Log name.
     * @param lev Log level.
     * @return Log record list.
     * @throws IOException An I/O exception.
     */
    List<LogRecord> query(Name name, Level lev) throws IOException;

    /**
     * Fetches all the log messages less than given log level.
     * @param name Log name.
     * @param lev Log level.
     * @return Log record list.
     * @throws IOException An I/O exception.
     */
    List<LogRecord> fetch(Name name, Level lev) throws IOException;

    /**
     * Queries all the log message after the specified timestamp.
     * @param n Log name.
     * @param l Log level.
     * @param t Timestamp.
     * @return Log record list.
     * @throws IOException An I/O exception.
     */
    List<LogRecord> queryAfter(Name n, Level l, long t) throws IOException;

    /**
     * Queries all the log message after the specified timestamp.
     * @param n Log name.
     * @param l Log level.
     * @param t Timestamp.
     * @return Log record list.
     * @throws IOException An I/O exception.
     */
    List<LogRecord> queryBefore(Name n, Level l, long t) throws IOException;
}
