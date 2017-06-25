package org.marid.db.dao;

/*-
 * #%L
 * marid-api
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface DaqWriter<T extends Serializable> extends DaqReader<T> {

    /**
     * Clears data within the time range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Count of removed records.
     */
    long delete(long from, long to);

    /**
     * Clears data within the time range for the given tags.
     * @param tags Tags.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Count of removed records.
     */
    long delete(long[] tags, long from, long to);

    /**
     * Merges data.
     * @param records Data records.
     * @param insertOnly Insert-only flag (if true, the values keep unchanged on key equality).
     * @return Merge result.
     */
    Set<DataRecordKey> merge(List<DataRecord<T>> records, boolean insertOnly);
}
