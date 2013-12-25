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

import org.marid.io.MaridSocketOptions;

import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultSocketConfigurer<S extends Socket> implements SocketConfigurer<S> {

    protected final Map<? extends SocketOption<?>, ?> socketOptions;

    public DefaultSocketConfigurer(Map<? extends SocketOption<?>, ?> socketOptions) {
        this.socketOptions = socketOptions;
    }

    @Override
    public void configure(S socket) throws Exception {
        for (final Map.Entry<? extends SocketOption<?>, ?> e : socketOptions.entrySet()) {
            if (e.getKey() == StandardSocketOptions.TCP_NODELAY) {
                socket.setTcpNoDelay((Boolean) e.getValue());
            } else if (e.getKey() == StandardSocketOptions.SO_SNDBUF) {
                socket.setSendBufferSize((Integer) e.getValue());
            } else if (e.getKey() == StandardSocketOptions.SO_RCVBUF) {
                socket.setReceiveBufferSize((Integer) e.getValue());
            } else if (e.getKey() == StandardSocketOptions.SO_LINGER) {
                final int linger = (Integer) e.getValue();
                if (linger < 0) {
                    socket.setSoLinger(false, 0);
                } else {
                    socket.setSoLinger(true, linger);
                }
            } else if (e.getKey() == StandardSocketOptions.SO_KEEPALIVE) {
                socket.setKeepAlive((Boolean) e.getValue());
            } else if (e.getKey() == StandardSocketOptions.SO_REUSEADDR) {
                socket.setReuseAddress((Boolean) e.getValue());
            } else if (e.getKey() == MaridSocketOptions.SO_TIMEOUT) {
                socket.setSoTimeout((Integer) e.getValue());
            }
        }
    }
}
