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
 * Fast array output stream.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class FastArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Default constructor.
     */
    public FastArrayOutputStream() {
    }

    /**
     * Constructs the fast array output stream.
     *
     * @param size Initial buffer size.
     */
    public FastArrayOutputStream(int size) {
        super(size);
    }

    /**
     * Get the shared input stream.
     *
     * @return Shared input stream.
     */
    public ByteArrayInputStream getSharedInputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }

    /**
     * Get the shared byte buffer.
     *
     * @return Shared byte buffer.
     */
    public ByteBuffer getSharedByteBuffer() {
        return ByteBuffer.wrap(buf, 0, count);
    }

    /**
     * Get the shared buffer.
     *
     * @return Shared buffer.
     */
    public byte[] getSharedBuffer() {
        return buf;
    }
}
