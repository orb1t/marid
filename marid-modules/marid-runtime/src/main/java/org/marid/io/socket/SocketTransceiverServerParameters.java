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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;
import java.util.Map;

import static org.marid.dyn.Casting.mapv;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiverServerParameters {

    private InetSocketAddress socketAddress = new InetSocketAddress(0);
    private int backlog = 50;

    public SocketTransceiverServerParameters() {
    }

    public SocketTransceiverServerParameters(Map<String, Object> p) {
        if (p.containsKey("host") && p.containsKey("port")) {
            socketAddress = new InetSocketAddress(mapv(p, "host", String.class), mapv(p, "port", int.class));
        } else if (p.containsKey("port")) {
            socketAddress = new InetSocketAddress(mapv(p, "port", int.class));
        }
    }

    public SocketTransceiverServerParameters setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
    }

    public SocketTransceiverServerParameters setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public int getBacklog() {
        return backlog;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
