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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.naming.Name;
import org.marid.io.NameIo;

/**
 * Abstract data record.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class DataRecord<T> implements Externalizable {

    private Name tag;
    private long time;

    /**
     * Default constructor.
     */
    public DataRecord() {
    }

    /**
     * Constructs the data record.
     *
     * @param tg Record tag.
     * @param ts Timestamp.
     */
    public DataRecord(Name tg, long ts) {
        if (tg == null) {
            throw new NullPointerException("Tag is null");
        }
        tag = tg;
        time = ts;
    }

    /**
     * Returns the record tag.
     *
     * @return Record tag.
     */
    public Name getTag() {
        return tag;
    }

    /**
     * Returns the record timestamp.
     *
     * @return Record timestamp.
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the record tag.
     *
     * @param tg Record tag.
     */
    public void setTag(Name tg) {
        if (tg == null) {
            throw new NullPointerException("Tag is null");
        }
        tag = tg;
    }

    /**
     * Sets the record timestamp.
     *
     * @param t Record timestamp.
     */
    public void setTime(long t) {
        time = t;
    }

    /**
     * Get the record code.
     * @return Record code.
     */
    public abstract byte getCode();

    @Override
    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        tag = NameIo.readName(in);
        time = in.readLong();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        NameIo.writeName(out, tag);
        out.writeLong(time);
    }
}
