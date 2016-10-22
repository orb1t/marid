/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class FastArrayOutputStream extends ByteArrayOutputStream {

    public FastArrayOutputStream() {
    }

    public FastArrayOutputStream(int size) {
        super(size);
    }

    public ByteArrayInputStream getSharedInputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }

    public ByteBuffer getSharedByteBuffer() {
        return ByteBuffer.wrap(buf, 0, count);
    }

    public byte[] getSharedBuffer() {
        return buf;
    }

    public ByteBuffer getTrimmedByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.wrap(buf);
        int offset = 0, limit = count;
        for (final byte b : buf) {
            if (Character.isWhitespace((char) b)) {
                offset++;
            } else {
                break;
            }
        }
        for (int i = count - 1; i >= 0; i--) {
            if (Character.isWhitespace((char) buf[i])) {
                limit--;
            } else {
                break;
            }
        }
        buffer.position(offset);
        buffer.limit(limit);
        return buffer;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size(); i++) {
            result = result * 31 + buf[i];
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FastArrayOutputStream)) {
            return false;
        }
        final FastArrayOutputStream that = (FastArrayOutputStream) obj;
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (this.buf[i] != that.buf[i]) {
                return false;
            }
        }
        return true;
    }
}
