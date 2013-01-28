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
package org.marid.db;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Properties;

/**
 * Data connection handler.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface DataConnectionHandler {
    /**
     * Connects to the remote side.
     * @param protocol Protocol (e.g. http, https, tcp).
     * @param address Socket address.
     * @param props Connection properties.
     * @param user User.
     * @param password Password.
     * @return Data connection.
     * @throws IOException An I/O exception.
     */
    public DataConnection connect(
            String protocol,
            SocketAddress address,
            Properties props,
            String user,
            char[] password) throws IOException;

    /**
     * Get the service protocol name.
     * @return Service protocol name.
     */
    public String getServiceProtocol();
}
