package org.marid.db.data;

import javax.annotation.Nonnull;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Arrays;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class DataRecord<T extends Serializable> implements Serializable {

    private final long tag;
    private final long timestamp;
    private final T value;

    @ConstructorProperties({"tag", "timestamp", "value"})
    public DataRecord(long tag, long timestamp, @Nonnull T value) {
        this.tag = tag;
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTag() {
        return tag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public T getValue() {
        return value;
    }

    public DataRecordKey getKey() {
        return new DataRecordKey(tag, timestamp);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[] {tag, timestamp, value});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataRecord) {
            final DataRecord<?> that = (DataRecord<?>) obj;
            return Arrays.deepEquals(
                    new Object[] {this.tag, this.timestamp, this.value},
                    new Object[] {that.tag, that.timestamp, that.value});
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String val = Arrays.deepToString(new Object[] {value});
        if (val.length() > 100) {
            val = val.substring(0, 100) + "...]";
        }
        return format("(%d, %tc, %s)", tag, timestamp, val);
    }
}
