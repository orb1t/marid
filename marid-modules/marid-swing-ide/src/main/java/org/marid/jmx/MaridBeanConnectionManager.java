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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridBeanConnectionManager implements LogSupport {

    private final Map<String, MaridBeanConnection> connectionMap = new ConcurrentHashMap<>();

    public void registerConnection(String name, MaridBeanConnection connection) {
        final MaridBeanConnection registered = connectionMap.computeIfPresent(name, (k, v) -> {
            try (final MaridBeanConnection old = v) {
                if (old != null) {
                    info("Closing {0}", old);
                }
            } catch (IOException x) {
                warning("Unable to close old connection for {0}", x, name);
            }
            return connection;
        });
        info("Registered bean connection {0} : {1}", name, registered);
    }

    public void unregisterConnection(String name) {
        connectionMap.computeIfPresent(name, (k, v) -> {
            try (final MaridBeanConnection c = v) {
                if (c != null) {
                    info("Closing {0}", c);
                }
            } catch (IOException x) {
                warning("Unable to close {0}", x, v);
            }
            return null;
        });
    }
}
