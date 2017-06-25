package org.marid.db.dao;

import org.marid.db.data.DataRecord;

import java.io.Serializable;
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
    long[] tags(long from, long to);

    /**
     * Get tag count within the given time range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Tag count.
     */
    long tagCount(long from, long to);

    /**
     * Get a tag record by tag and timestamp.
     * @param tag Record tag.
     * @param instant Timestamp.
     * @return Data record.
     */
    DataRecord<T> fetchRecord(long tag, long instant);

    /**
     * Fetches all the records within the given time range.
     * @param tags Tags to be fetched.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @return Fetched records.
     */
    List<DataRecord<T>> fetchRecords(long[] tags, long from, long to);

    /**
     * Hashes record range.
     * @param from Lower bound (inclusive).
     * @param to Upper bound (exclusive).
     * @param includeData Whether include data or not.
     * @param algorithm Digest algorithm (e.g. SHA-1 or MD5).
     * @return Hashes.
     */
    Map<Long, String> hash(long from, long to, boolean includeData, String algorithm);
}
