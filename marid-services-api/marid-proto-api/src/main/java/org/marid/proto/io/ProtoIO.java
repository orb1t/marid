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

import java.io.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoIO extends Closeable {

    PushbackInputStream getPushbackInputStream();

    DataInputStream getDataInputStream();

    DataOutputStream getDataOutputStream();

    default void doWith(IOBiConsumer<DataInputStream, DataOutputStream> consumer) throws IOException {
        consumer.ioAccept(getDataInputStream(), getDataOutputStream());
    }

    default <T> T call(IOBiFunction<DataInputStream, DataOutputStream, T> function) throws IOException {
        return function.ioApply(getDataInputStream(), getDataOutputStream());
    }
}
