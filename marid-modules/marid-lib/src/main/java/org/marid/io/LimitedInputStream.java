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

package org.marid.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry Ovchinnikov
 */
public class LimitedInputStream extends InputStream {

    private final InputStream source;
    private long limit;

    public LimitedInputStream(InputStream source, long limit) {
        this.source = source;
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        if (limit > 0) {
            final int r = source.read();
            if (r < 0) {
                return r;
            } else {
                limit--;
                return r;
            }
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (limit > 0) {
            if (limit < len) {
                final int r = source.read(b, off, (int) limit);
                if (r < 0) {
                    return r;
                } else {
                    limit -= r;
                    return r;
                }
            } else {
                final int r = source.read(b, off, len);
                if (r < 0) {
                    return r;
                } else {
                    limit -= r;
                    return r;
                }
            }
        } else {
            return -1;
        }
    }
}
