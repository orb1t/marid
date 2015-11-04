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

import org.marid.io.BinStreams;
import org.marid.io.Transceiver;
import org.marid.io.TransceiverServer;
import org.marid.logging.LogSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiverServer implements TransceiverServer, LogSupport {

    private final InetSocketAddress address;
    private final int backlog;

    private ServerSocket serverSocket;

    public SocketTransceiverServer(SocketTransceiverServerParameters parameters) {
        address = parameters.socketAddress();
        backlog = parameters.backlog();
    }

    @Override
    public void open() throws IOException {
        if (serverSocket == null) {
            serverSocket = new ServerSocket(address.getPort(), backlog, address.getAddress());
        }
    }

    @Override
    public Transceiver accept() throws IOException {
        final ServerSocket ss = serverSocket;
        Socket socket = null;
        InputStream inputStream = null;
        try {
            socket = ss.accept();
            inputStream = socket.getInputStream();
            return new Client(socket, inputStream, socket.getOutputStream());
        }  catch (SocketException x) {
            try (final Socket s = socket; final InputStream is = inputStream) {
                assert s != null && !s.isClosed() || s == null;
                assert is != null && is.available() >= 0 || is == null;
            }
            if (ss.isClosed()) {
                final ClosedChannelException cx = new ClosedChannelException();
                cx.initCause(x);
                throw cx;
            } else {
                throw x;
            }
        }
    }

    public int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : address.getPort();
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
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public Client(Socket socket, InputStream inputStream, OutputStream outputStream) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public boolean isValid() {
            return !socket.isClosed();
        }

        @Override
        public void open() throws IOException {
        }

        @Override
        public void write(byte[] data, int offset, int len) throws IOException {
            BinStreams.invoke(socket, () -> {
                outputStream.write(data, offset, len);
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
            }
        }
    }
}
