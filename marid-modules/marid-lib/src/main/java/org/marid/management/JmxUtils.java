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

package org.marid.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;

/**
 * @author Dmitry Ovchinnikov
 */
public class JmxUtils {

    public static void startServer(JMXConnectorServer server, JMXServiceURL url) throws IOException {
        if (server instanceof RMIConnectorServer) {
            final String urlPath = url.getURLPath();
            if (urlPath.startsWith("/jndi/")) {
                try {
                    final URI uri = new URI(urlPath.substring(6));
                    LocateRegistry.createRegistry(uri.getPort());
                    server.start();
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }
        }
        server.start();
    }

    public static ObjectName getObjectName(String domain, String... keyValues) {
        final Hashtable<String, String> hashtable = new Hashtable<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            hashtable.put(keyValues[i], i + 1 < keyValues.length ? keyValues[i + 1] : null);
        }
        try {
            return new ObjectName(domain, hashtable);
        } catch (MalformedObjectNameException x) {
            throw new IllegalArgumentException(domain + hashtable, x);
        }
    }

    public static ObjectName getObjectName(Class<?> type) {
        try {
            return new ObjectName(type.getPackage().getName(), "name", type.getSimpleName());
        } catch (MalformedObjectNameException x) {
            throw new IllegalArgumentException(type.getName(), x);
        }
    }
}
