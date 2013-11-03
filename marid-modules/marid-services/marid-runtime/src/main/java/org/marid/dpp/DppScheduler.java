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

package org.marid.dpp;

import org.marid.methods.PropMethods;
import org.marid.tree.StaticTreeObject;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.emptyMap;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppScheduler extends StaticTreeObject {

    protected final boolean logDurations;

    public DppScheduler(String name, Map params) {
        super(null, name, PropMethods.get(params, Map.class, "vars", emptyMap()));
        logDurations = PropMethods.get(params, boolean.class, "logDurations", true);
        final Map buses = PropMethods.get(params, Map.class, "buses", emptyMap());
        for (final Object e : buses.entrySet()) {
            final Entry entry = (Entry) e;
            final String busName = String.valueOf(entry.getKey());
            final Map busParams = PropMethods.get(buses, Map.class, entry.getKey(), emptyMap());
            children.put(busName, new DppBus(this, busName, busParams));
            info(logger, "Added bus {0}", busName);
        }
    }

    public void start() {
        for (final StaticTreeObject child : children.values()) {
            if (child instanceof DppBus) {
                ((DppBus) child).start();
            }
        }
    }

    public void stop() {
        for (final StaticTreeObject child : children.values()) {
            if (child instanceof DppBus) {
                ((DppBus) child).stop();
            }
        }
        children.clear();
    }
}
