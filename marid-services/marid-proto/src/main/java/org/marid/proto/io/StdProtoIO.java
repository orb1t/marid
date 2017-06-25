/*-
 * #%L
 * marid-proto
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.proto.io;

import java.io.*;
import java.util.logging.Level;

import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoIO implements ProtoIO, Closeable {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public StdProtoIO(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void close() throws IOException {
        try (final InputStream is = inputStream; final OutputStream os = outputStream) {
            log(Level.CONFIG, "Closing {0} and {1}", is, os);
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
