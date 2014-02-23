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
import org.marid.collections.MapUtils;

import java.io.PrintWriter;

/**
 * @author Dmitry Ovchinnikov
 */
public class HsqldbServerProvider {

    public static Server getServer(HsqldbConfiguration configuration, PrintWriter logWriter, PrintWriter errWriter) {
        final Server server = configuration.getProtocol().newServerInstance();
        server.setLogWriter(logWriter);
        server.setErrWriter(errWriter);
        server.setTls(configuration.getProtocol().isTls());
        server.setAddress(configuration.getHost());
        server.setPort(configuration.getPort());
        server.setDaemon(configuration.isDaemon());
        server.setSilent(configuration.isSilent());
        server.setTrace(configuration.isTrace());
        server.setRestartOnShutdown(configuration.isRestartOnShutdown());
        server.setDefaultWebPage(configuration.getDefaultWebPage());
        server.setNoSystemExit(configuration.isNoSystemExit());
        MapUtils.forEach(configuration.getDatabaseMap(), (index, entry) -> {
            server.setDatabaseName(index, entry.getKey());
            server.setDatabasePath(index, entry.getValue());
        });
        return server;
    }
}
