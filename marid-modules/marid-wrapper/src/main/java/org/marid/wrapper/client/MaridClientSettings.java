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

package org.marid.wrapper.client;

import org.marid.net.SocketConfigurer;
import org.marid.net.StandardSocketConfigurer;
import org.marid.secure.SecureProfile;
import org.marid.wrapper.WrapperConstants;

import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridClientSettings {

    private SecureProfile secureProfile = SecureProfile.DEFAULT;
    private InetSocketAddress address = new InetSocketAddress(WrapperConstants.DEFAULT_PORT);
    private int mode = WrapperConstants.MODE_DEFAULT;
    private final Map<SocketOption<?>, Object> socketOptions = new HashMap<>();

    public SecureProfile getSecureProfile() {
        return secureProfile;
    }

    public MaridClientSettings setSecureProfile(SecureProfile secureProfile) {
        this.secureProfile = secureProfile;
        return this;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public MaridClientSettings setAddress(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        return this;
    }

    public int getMode() {
        return mode;
    }

    public MaridClientSettings setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public <V, O extends SocketOption<V>> MaridClientSettings setSocketOption(O option, V value) {
        socketOptions.put(option, value);
        return this;
    }

    public Map<? extends SocketOption<?>, ?> getSocketOptions() {
        return socketOptions;
    }

    public SocketConfigurer<SSLSocket> socketConfigurer() {
        return new StandardSocketConfigurer<>(address, socketOptions);
    }
}
