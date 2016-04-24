/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic DAQ DAO.
 *
 * @param <T> Data record type.
 *
 * @author Dmitry Ovchinnikov.
 */
public interface DaqReader<T extends Serializable> extends DaqMXBean, AutoCloseable {

    /**
     * Get all tags within the given time range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Tag set.
     */
    long[] tags(Instant from, Instant to);

    /**
     * Get tag count within the given time range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Tag count.
     */
    long tagCount(Instant from, Instant to);

    /**
     * Get a tag record by tag and timestamp.
     * @param tag Record tag.
     * @param instant Timestamp.
     * @return Data record.
     */
    DataRecord<T> fetchRecord(long tag, Instant instant);

    /**
     * Fetches all the records within the given time range.
     * @param tags Tags to be fetched.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Fetched records.
     */
    List<DataRecord<T>> fetchRecords(long[] tags, Instant from, Instant to);

    /**
     * Hashes record range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @param includeData Whether include data or not.
     * @param algorithm Digest algorithm (e.g. SHA-1 or MD5).
     * @return Hashes.
     */
    Map<Long, String> hash(Instant from, Instant to, boolean includeData, String algorithm);
}
