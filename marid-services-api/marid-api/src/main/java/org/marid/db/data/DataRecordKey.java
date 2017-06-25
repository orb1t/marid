package org.marid.db.data;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class DataRecordKey {

    private final long tag;
    private final long timestamp;

    public DataRecordKey(long tag, long timestamp) {
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public long getTag() {
        return tag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataRecordKey) {
            final DataRecordKey that = (DataRecordKey) obj;
            return tag == that.tag && timestamp == that.timestamp;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s)", getClass().getSimpleName(), tag, timestamp);
    }
}
