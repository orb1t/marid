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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

import static org.marid.pref.PrefCodecs.mapv;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiverParameters {

    private InetSocketAddress socketAddress = new InetSocketAddress(0);
    private Proxy proxy = Proxy.NO_PROXY;
    private int connectTimeout = 10_000;
    private int soTimeout = 10_000;
    private boolean reuseAddress = true;

    public SocketTransceiverParameters() {
    }

    public SocketTransceiverParameters(Map<String, Object> p) {
        if (p.containsKey("host") && p.containsKey("port")) {
            socketAddress = new InetSocketAddress(mapv(p, "host", String.class), mapv(p, "port", int.class));
        } else if (p.containsKey("address") && p.containsKey("port")) {
            socketAddress = new InetSocketAddress(mapv(p, "address", InetAddress.class), mapv(p, "port", int.class));
        } else if (p.containsKey("socketAddress")) {
            socketAddress = mapv(p, "socketAddress", InetSocketAddress.class);
        }
        if (p.containsKey("proxyType") && p.containsKey("proxyHost") && p.containsKey("proxyPort")) {
            proxy = new Proxy(
                    mapv(p, "proxyType", Proxy.Type.class),
                    new InetSocketAddress(mapv(p, "proxyHost", String.class), mapv(p, "proxyPort", int.class)));
        }
        if (p.containsKey("connectTimeout")) {
            connectTimeout = mapv(p, "connectTimeout", int.class);
        }
        if (p.containsKey("soTimeout")) {
            soTimeout = mapv(p, "soTimeout", int.class);
        }
        if (p.containsKey("reuseAddress")) {
            reuseAddress = mapv(p, "reuseAddress", boolean.class);
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public SocketTransceiverParameters setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public SocketTransceiverParameters setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        return this;
    }

    public SocketTransceiverParameters setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public SocketTransceiverParameters setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public SocketTransceiverParameters setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
