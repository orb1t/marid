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

package org.marid.jmx;

import org.marid.logging.LogSupport;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class MaridBeanConnectionManager implements LogSupport {

    private final Map<String, MBeanServerConnection> connectionMap = new ConcurrentHashMap<>();

    public void registerConnection(String name, MBeanServerConnection connection) {
        connectionMap.put(name, connection);
    }

    public void unregisterConnection(String name) {
        connectionMap.remove(name);
    }

    public MBeanServerConnection getConnection(String name) {
        return connectionMap.get(name);
    }

    public Set<String> getConnectionNames() {
        return Collections.unmodifiableSet(connectionMap.keySet());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
