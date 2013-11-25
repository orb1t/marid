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

package org.marid.wrapper.deploy;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClientInfo {

    private final InetAddress address;
    private ClientData clientData;

    public ClientInfo(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    public ClientData getClientData() {
        return clientData;
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("address", address);
        if (clientData != null) {
            map.put("javaVersion", clientData.getJvmProperties().get("java.version"));
            map.put("javaVendor", clientData.getJvmProperties().get("java.vendor"));
        }
        return getClass().getSimpleName() + map;
    }
}
