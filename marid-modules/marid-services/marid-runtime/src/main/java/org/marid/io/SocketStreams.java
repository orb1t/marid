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

package org.marid.io;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static org.marid.methods.PropMethods.getInetSocketAddress;
import static org.marid.methods.PropMethods.get;

/**
 * @author Dmitry Ovchinnikov
 */
public class SocketStreams extends Socket implements IOStreams {

    public SocketStreams(Map params) throws IOException {
        connect(getInetSocketAddress(params, "host", 502), get(params, int.class, "ctimeout", 0));
        setSoTimeout(get(params, int.class, "timeout", 60_000));
        setKeepAlive(get(params, boolean.class, "keepAlive", getKeepAlive()));
        setReceiveBufferSize(get(params, int.class, "rcvBufSize", getReceiveBufferSize()));
        setSendBufferSize(get(params, int.class, "sendBufSize", getSendBufferSize()));
        setOOBInline(get(params, boolean.class, "oobInline", getOOBInline()));
        setReuseAddress(get(params, boolean.class, "reuseAddress", getReuseAddress()));
        setTrafficClass(get(params, int.class, "trafficClass", getTrafficClass()));
        setTcpNoDelay(get(params, boolean.class, "tcpNoDelay", getTcpNoDelay()));
    }

    @Override
    public boolean isValid() {
        return isConnected() && !isClosed();
    }
}
