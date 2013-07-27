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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Metered output stream.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MeteredOutputStream extends OutputStream implements MeteredWrite {

    private long count;
    private OutputStream stream;

    /**
     * Constructs the metered output stream.
     *
     * @param os Wrapped output stream.
     */
    public MeteredOutputStream(OutputStream os) {
        stream = os;
    }

    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
        count += b.length;
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
        count += len;
    }

    @Override
    public long getWrittenCount() {
        return count;
    }

    @Override
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            stream = null;
        }
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }
}
