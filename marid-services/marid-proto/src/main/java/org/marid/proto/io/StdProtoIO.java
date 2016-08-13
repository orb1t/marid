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

import java.io.*;
import java.util.logging.Level;

import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoIO implements ProtoIO, Closeable {

    private final PushbackInputStream pushbackInputStream;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    public StdProtoIO(InputStream inputStream, OutputStream outputStream, int pushbackSize) throws IOException {
        this.pushbackInputStream = new PushbackInputStream(inputStream, pushbackSize);
        this.dataInputStream = new DataInputStream(pushbackInputStream);
        this.dataOutputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void close() throws IOException {
        try (final InputStream is = dataInputStream; final OutputStream os = dataOutputStream) {
            log(Level.CONFIG, "Closing {0} and {1}", is, os);
        }
    }

    @Override
    public PushbackInputStream getPushbackInputStream() {
        return pushbackInputStream;
    }

    @Override
    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    @Override
    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
