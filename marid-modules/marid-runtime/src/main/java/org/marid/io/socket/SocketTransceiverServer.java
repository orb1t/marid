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
import org.marid.io.TransceiverServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiverServer implements TransceiverServer {

    private final InetSocketAddress address;
    private final int backlog;

    private ServerSocket serverSocket;

    public SocketTransceiverServer(SocketTransceiverServerParameters parameters) {
        address = parameters.getSocketAddress();
        backlog = parameters.getBacklog();
    }

    @Override
    public Transceiver accept() throws IOException {
        if (serverSocket == null) {
            serverSocket = new ServerSocket(address.getPort(), backlog, address.getAddress());
        }
        return new Client(serverSocket.accept());
    }

    @Override
    public void close() throws IOException {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } finally {
            serverSocket = null;
        }
    }

    public class Client implements Transceiver {

        private final Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public Client(Socket socket) throws IOException {
            this.socket = socket;
        }

        @Override
        public synchronized boolean isValid() {
            return !socket.isClosed();
        }

        @Override
        public synchronized void open() throws IOException {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
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
            try (final Socket s = socket; final InputStream is = inputStream; final OutputStream os = outputStream) {
                assert s != null;
                assert is != null && os != null || is == null && os == null;
            }
        }

        public Socket getSocket() {
            return socket;
        }
    }
}
