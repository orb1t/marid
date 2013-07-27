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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

/**
 * Bytes data record.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class BytesDataRecord extends ObjectDataRecord<byte[]> {
    /**
     * Constructs the byte record.
     */
    public BytesDataRecord() {
        this("", 0L, new byte[0]);
    }

    /**
     * Constructs the bytes data record.
     * @param tag Tag.
     * @param ts Timestamp.
     * @param val Value.
     */
    public BytesDataRecord(String tag, long ts, byte[] val) {
        super(tag, ts, val);
    }

    @Override
    public BytesDataRecord clone() {
        return new BytesDataRecord(getTag(), getTime(), getValue());
    }

    /**
     * Get the byte buffer.
     * @return Byte buffer.
     */
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(getValue());
    }

    /**
     * Get the input stream.
     * @return Input stream.
     */
    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(getValue());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(getValue().length);
        out.write(getValue());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        super.readExternal(in);
        byte[] value = new byte[in.readInt()];
        in.readFully(value);
        setValue(value);
    }
}
