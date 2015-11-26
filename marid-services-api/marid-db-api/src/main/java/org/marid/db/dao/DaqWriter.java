/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.db.dao;

import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;

import java.io.Serializable;
import java.time.Instant;
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
    long delete(Instant from, Instant to);

    /**
     * Clears data within the time range for the given tags.
     * @param tags Tags.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Count of removed records.
     */
    long delete(Set<String> tags, Instant from, Instant to);

    /**
     * Merges data.
     * @param records Data records.
     * @param insertOnly Insert-only flag (if true, the values keep unchanged on key equality).
     * @return Merge result.
     */
    Set<DataRecordKey> merge(List<DataRecord<T>> records, boolean insertOnly);
}
