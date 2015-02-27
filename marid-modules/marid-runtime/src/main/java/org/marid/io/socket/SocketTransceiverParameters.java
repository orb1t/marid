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
import java.net.Proxy;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SocketTransceiverParameters {

    private InetSocketAddress socketAddress = new InetSocketAddress(0);
    private Proxy proxy = Proxy.NO_PROXY;
    private int connectTimeout = 10_000;
    private int soTimeout = 10_000;
    private boolean reuseAddress = true;

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
