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

package org.marid.net;

import javax.net.SocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class SocketWrapper<S extends Socket, I extends InputStream, O extends OutputStream> implements Closeable {

    protected final S socket;
    protected final I input;
    protected final O output;

    @SuppressWarnings("unchecked")
    public SocketWrapper(SocketFactory socketFactory, SocketConfigurer<S> configurer) throws IOException {
        socket = (S) socketFactory.createSocket();
        try {
            configurer.configure(socket);
        } catch (Exception x) {
            try {
                socket.close();
            } catch (Exception y) {
                x.addSuppressed(y);
            }
            if (x instanceof IOException) {
                throw (IOException) x;
            } else {
                throw new IllegalStateException(x);
            }
        }
        final InputStream inputStream;
        try {
            inputStream = socket.getInputStream();
        } catch (Exception x) {
            try {
                close();
            } catch (Exception y) {
                x.addSuppressed(y);
            }
            throw x;
        }
        final OutputStream outputStream;
        try {
            outputStream = socket.getOutputStream();
        } catch (Exception x) {
            try (final S s = socket; final InputStream i = inputStream) {
                assert s != null && i != null;
            } catch (Exception y) {
                x.addSuppressed(y);
            }
            throw x;
        }
        final I in;
        try {
            in = getInput(inputStream);
        } catch (Exception x) {
            try (final S s = socket; final InputStream i = inputStream; OutputStream o = outputStream) {
                assert s != null && i != null && o != null;
            } catch (Exception y) {
                x.addSuppressed(y);
            }
            throw x;
        }
        final O out;
        try {
            out = getOutput(outputStream);
        } catch (Exception x) {
            try (final S s = socket; final InputStream i = inputStream; OutputStream o = outputStream; I inp = in) {
                assert s != null && i != null && o != null && inp != null;
            } catch (Exception y) {
                x.addSuppressed(y);
            }
            throw x;
        }
        input = in;
        output = out;
    }

    protected abstract I getInput(InputStream inputStream) throws IOException;

    protected abstract O getOutput(OutputStream outputStream) throws IOException;

    @Override
    public void close() throws IOException {
        try (final S s = socket; final I i = input; final O o = output) {
            assert s != null || i != null || o != null;
        }
    }
}
