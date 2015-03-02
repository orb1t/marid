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

import org.marid.io.Transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.AlreadyConnectedException;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiver implements Transceiver {

    private final SocketTransceiverParameters parameters;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SocketTransceiver(SocketTransceiverParameters parameters) {
        this.parameters = parameters;
    }

    public SocketTransceiver(Map<String, Object> parameters) {
        this(new SocketTransceiverParameters(parameters));
    }

    @Override
    public synchronized boolean isValid() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public synchronized void open() throws IOException {
        if (socket == null) {
            socket = new Socket(parameters.getProxy());
            try {
                socket.setSoTimeout(parameters.getSoTimeout());
                socket.setReuseAddress(parameters.isReuseAddress());
                socket.connect(parameters.getSocketAddress(), parameters.getConnectTimeout());
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
    public synchronized void write(byte[] data, int offset, int len) throws IOException {
        outputStream.write(data, offset, len);
    }

    @Override
    public synchronized int read(byte[] data, int offset, int len) throws IOException {
        return inputStream.read(data, offset, len);
    }

    @Override
    public synchronized int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public synchronized void close() throws IOException {
        try (final InputStream is = inputStream; final OutputStream os = outputStream; final Socket s = socket) {
            assert s != null && is != null && os == null || s == null && os == null && is == null;
        } finally {
            inputStream = null;
            outputStream = null;
            socket = null;
        }
    }
}
