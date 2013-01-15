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
import javax.naming.Name;

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
    public int insert(List<List> rows) throws IOException;

    /**
     * Appends rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int append(List<List> rows) throws IOException;

    /**
     * Updates rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int update(List<List> rows) throws IOException;

    /**
     * Updates rows of data.
     *
     * @param rows Rows.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(List<List> rows) throws IOException;

    /**
     * Queries the DB.
     *
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> query(Name tag, long ts) throws IOException;

    /**
     * Queries the DB.
     *
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> query(List<Name> tags, long ts) throws IOException;

    /**
     * Queries the DB after the given timestamp.
     *
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> queryAfter(Name tag, long ts) throws IOException;

    /**
     * Queries the DB after the given timestamp.
     *
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> queryAfter(List<Name> tags, long ts) throws IOException;

    /**
     * Queries the DB before the given timestamp.
     *
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> queryBefore(Name tag, long ts) throws IOException;

    /**
     * Queries the DB before the given timestamp.
     *
     * @param tags Tag.
     * @param ts Timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> queryBefore(List<Name> tags, long ts) throws IOException;

    /**
     * Queries the DB between the given timestamps.
     * @param tag Tag.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> query(Name tag, long ts, long tf) throws IOException;

    /**
     * Queries the DB between the given timestamps.
     * @param tl Tags.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Data rows.
     * @throws IOException An I/O exception.
     */
    public List<List> query(List<Name> tl, long ts, long tf) throws IOException;
}
