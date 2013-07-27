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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ServiceLoader;

/**
 * Data connection factory.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public final class DataConnectionFactory {
    /**
     * Obtain a data connection.
     * @param uri Remote resource URI.
     * @return Data connection.
     */
    public static DataConnection connect(URI uri) throws IOException {
        DataConnectionHandler handler = null;
        synchronized(DataConnectionFactory.class) {
            String serviceProtocol = uri.getScheme();
            if (serviceProtocol == null) {
                throw new IllegalArgumentException("Service protocol is null");
            }
            try {
                uri = new URI(uri.getRawSchemeSpecificPart());
            } catch (URISyntaxException x) {
                throw new IllegalArgumentException(x);
            }
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("Host is null");
            }
            for (DataConnectionHandler h : Loader.loader) {
                if (serviceProtocol.equals(h.getServiceProtocol())) {
                    handler = h;
                    break;
                }
            }
        }
        if (handler != null) {
            return handler.connect(uri.getScheme(), uri);
        } else {
            throw new IllegalStateException("No such service");
        }
    }

    private static class Loader {

        private static final ServiceLoader<DataConnectionHandler> loader =
                ServiceLoader.load(DataConnectionHandler.class);
    }
}
