/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.wrapper.hsqldb;

import org.hsqldb.server.Server;
import org.hsqldb.server.WebServer;

/**
 * @author Dmitry Ovchinnikov
 */
public enum HsqldbProtocol {
    HSQL("hsql", false),
    HSQLS("hsqls", true),
    HTTP("http", false),
    HTTPS("https", true);

    private final String protocol;
    private final boolean tls;

    private HsqldbProtocol(String protocol, boolean tls) {
        this.protocol = protocol;
        this.tls = tls;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isTls() {
        return tls;
    }

    public Server newServerInstance() {
        final Server server;
        switch (protocol) {
            case "hsql":
            case "hsqls":
                server = new Server();
                break;
            case "http":
            case "https":
                server = new WebServer();
                break;
            default:
                throw new IllegalStateException(protocol);
        }
        return server;
    }
}
