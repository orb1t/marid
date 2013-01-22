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
package org.marid.db.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Long data record.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class LongDataRecord extends NumericDataRecord<Long> {

    private long value;

    /**
     * Default constructor.
     */
    public LongDataRecord() {
        this(null, 0L, 0L);
    }

    /**
     * Constructs the long data record.
     * @param tag Tag.
     * @param ts Timestamp.
     * @param val Value.
     */
    public LongDataRecord(String tag, long ts, long val) {
        super(tag, ts);
        value = val;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public void setValue(Long val) {
        if (val == null) {
            throw new NullPointerException("Value is null");
        }
        value = val.longValue();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        super.readExternal(in);
        value = in.readLong();
    }

    @Override
    public DataRecord<Long> clone() {
        return new LongDataRecord(getTag(), getTime(), value);
    }
}
