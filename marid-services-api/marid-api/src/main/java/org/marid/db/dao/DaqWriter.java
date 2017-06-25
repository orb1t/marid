package org.marid.db.dao;

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
