/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Dmitry Ovchinnikov
 */
public class AppendableWriter extends Writer {

    private final Appendable appendable;

    public AppendableWriter(@Nonnull Appendable appendable) {
        this.appendable = appendable;
    }

    @Override
    public void write(@Nonnull char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            appendable.append(cbuf[i + off]);
        }
    }

    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }
}