/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.io.socket;

import org.marid.groovy.MapProxies;
import org.marid.io.BinStreams;
import org.marid.io.Transceiver;
import org.marid.logging.LogSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.AlreadyConnectedException;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiver implements Transceiver, LogSupport {

    private final SocketTransceiverParameters parameters;
    private volatile Socket socket;
    private volatile InputStream inputStream;
    private volatile OutputStream outputStream;

    public SocketTransceiver(SocketTransceiverParameters parameters) {
        this.parameters = parameters;
    }

    public SocketTransceiver(Map map) {
        this(MapProxies.newInstance(SocketTransceiverParameters.class, map));
    }

    @Override
    public boolean isValid() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void open() throws IOException {
        if (socket == null) {
            socket = new Socket(parameters.proxy());
            try {
                socket.setSoTimeout(parameters.soTimeout());
                socket.setReuseAddress(parameters.reuseAddress());
                socket.connect(parameters.socketAddress(), parameters.connectTimeout());
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException x) {
                try (final Socket s = socket) {
                    assert s == socket;
                } catch (IOException suppressed) {
                    x.addSuppressed(suppressed);
                } finally {
                    inputStream = null;
                    outputStream = null;
                    socket = null;
                }
                throw x;
            }
        } else {
            throw new AlreadyConnectedException();
        }
    }

    @Override
    public void write(byte[] data, int offset, int len) throws IOException {
        BinStreams.invoke(socket, () -> {
            outputStream.write(data, offset, len);
            outputStream.flush();
            return null;
        });
    }

    @Override
    public int read(byte[] data, int offset, int len) throws IOException {
        return BinStreams.invoke(socket, () -> inputStream.read(data, offset, len));
    }

    @Override
    public int available() throws IOException {
        return BinStreams.invoke(socket, inputStream::available);
    }

    @Override
    public void close() throws IOException {
        try (final Socket s = socket; final InputStream is = inputStream; final OutputStream os = outputStream) {
            log(FINEST, "close {0} {1} {2}", s, is, os);
        } finally {
            inputStream = null;
            outputStream = null;
            socket = null;
        }
    }
}
