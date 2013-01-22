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
import java.util.Map;
import org.marid.db.data.DataRecord;

/**
 * Data archive storage.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface ArchiveStorage extends HistoricalStorage {
    /**
     * Inserts rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int insert(List<? extends DataRecord> rows) throws IOException;

    /**
     * Appends rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int append(List<? extends DataRecord> rows) throws IOException;

    /**
     * Updates rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int update(List<? extends DataRecord> rows) throws IOException;

    /**
     * Updates rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(List<? extends DataRecord> rows) throws IOException;

    /**
     * Queries the DB.
     *
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            query(String tag, long ts) throws IOException;

    /**
     * Queries the DB.
     *
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            query(List<String> tags, long ts) throws IOException;

    /**
     * Queries the DB.
     *
     * @param ss Snapshot.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            query(Map<String, Long> ss) throws IOException;

    /**
     * Queries the DB after the given timestamp.
     *
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<DataRecord>
            queryAfter(String tag, long ts) throws IOException;

    /**
     * Queries the DB after the given timestamp.
     *
     * @param <T> Data record type.
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            queryAfter(List<String> tags, long ts) throws IOException;

    /**
     * Queries the DB after.
     *
     * @param <T> Data record type.
     * @param ss Snapshot.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            queryAfter(Map<String, Long> ss) throws IOException;

    /**
     * Queries the DB before the given timestamp.
     *
     * @param <T> Data record type.
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            queryBefore(String tag, long ts) throws IOException;

    /**
     * Queries the DB before the given timestamp.
     *
     * @param <T> Data record type.
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            queryBefore(List<String> tags, long ts) throws IOException;

    /**
     * Queries the DB before.
     *
     * @param <T> Data record type.
     * @param ss Snapshot.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord>
            List<T> queryBefore(Map<String, Long> ss) throws IOException;

    /**
     * Queries the DB between the given timestamps.
     *
     * @param <T> Record type.
     * @param tag Tag.
     * @param t1 Start timestamp.
     * @param t2 Final timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T>
            query(String tag, long t1, long t2) throws IOException;

    /**
     * Queries the DB between the given timestamps.
     *
     * @param <T> Record type.
     * @param tl Tags.
     * @param t1 Start timestamp.
     * @param t2 Final timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T> query(
            List<String> tl,
            long t1, long t2) throws IOException;

    /**
     * Queries the DB between.
     *
     * @param <T> Record type.
     * @param s1 Start snapshot.
     * @param s2 Final snapshot.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public <T extends DataRecord> List<T> query(
            Map<String, Long> s1,
            Map<String, Long> s2) throws IOException;

    /**
     * Get the last snapshot.
     *
     * @param tags Tags.
     * @return Snapshot.
     * @throws IOException An I/O exception.
     */
    public Map<String, Long>
            getLastSnapshot(List<String> tags) throws IOException;

    /**
     * Get the first snapshot.
     *
     * @param tags Tags.
     * @return Snapshot.
     * @throws IOException An I/O exception.
     */
    public Map<String, Long>
            getFirstSnapshot(List<String> tags) throws IOException;
}
