/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.proto.io;

import org.marid.io.IOBiConsumer;
import org.marid.io.IOBiFunction;
import org.marid.io.IOCloseable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoIO extends IOCloseable {

    InputStream getInputStream();

    OutputStream getOutputStream();

    default void doWith(IOBiConsumer<InputStream, OutputStream> consumer) throws IOException {
        consumer.accept(getInputStream(), getOutputStream());
    }

    default <T> T call(IOBiFunction<InputStream, OutputStream, T> function) throws IOException {
        return function.apply(getInputStream(), getOutputStream());
    }
}
