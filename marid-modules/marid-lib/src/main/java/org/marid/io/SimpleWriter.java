/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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
import java.io.Writer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleWriter extends Writer {

    protected final Consumer<SimpleWriter> flusher;
    protected final Consumer<SimpleWriter> closer;
    protected final BiConsumer<SimpleWriter, String> writer;

    public SimpleWriter(Consumer<SimpleWriter> flusher, Consumer<SimpleWriter> closer, BiConsumer<SimpleWriter, String> writer) {
        this.flusher = flusher;
        this.closer = closer;
        this.writer = writer;
    }

    public SimpleWriter(Consumer<SimpleWriter> closer, BiConsumer<SimpleWriter, String> writer) {
        this(w -> {}, closer, writer);
    }

    public SimpleWriter(BiConsumer<SimpleWriter, String> writer) {
        this(w -> {}, writer);
    }

    @Override
    public void write(@Nonnull char[] cbuf, int off, int len) {
        writer.accept(this, new String(cbuf, off, len));
    }

    @Override
    public void flush() {
        flusher.accept(this);
    }

    @Override
    public void close() {
        closer.accept(this);
    }
}
