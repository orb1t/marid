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

package org.marid.service;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ServiceManager;

import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static java.util.ServiceLoader.load;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridServices {

    private static final Logger LOG = Logger.getLogger(MaridServices.class.getName());
    private static final Multimap<String, MaridService> TS_MAP = LinkedListMultimap.create();
    private static final Map<String, MaridService> IS_MAP = new HashMap<>();
    private static final ServiceManager SERVICE_MANAGER;

    static {
        try {
            for (final MaridServiceProvider provider : load(MaridServiceProvider.class)) {
                try {
                    for (final MaridService service : provider.getServices()) {
                        TS_MAP.put(service.type(), service);
                        final MaridService oldService = IS_MAP.put(service.id(), service);
                        if (oldService != null) {
                            warning(LOG, "Duplicate service for id = {0}", service.id());
                        }
                        info(LOG, "{0} Added", service);
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to get services from {0}", x, provider);
                }
            }
        } catch (Exception x) {
            severe(LOG, "Unable to load services", x);
        }
        SERVICE_MANAGER = new ServiceManager(IS_MAP.values());
    }

    public static Collection<MaridService> getServices() {
        return Collections.unmodifiableCollection(IS_MAP.values());
    }

    public static MaridService getServiceById(String id) {
        return IS_MAP.get(id);
    }

    public static MaridService getServiceByType(String type) {
        final Collection<MaridService> services = TS_MAP.get(type);
        if (services == null) {
            return null;
        } else {
            final Iterator<MaridService> it = services.iterator();
            return it.hasNext() ? it.next() : null;
        }
    }

    public static Collection<MaridService> getServicesByType(String type) {
        return TS_MAP.get(type);
    }

    public static <T> Future<T> send(String type, String method, Object... args) {
        final MaridService service = getServiceByType(type);
        if (service != null) {
            return service.send(method, args);
        } else {
            throw new IllegalArgumentException("Service not exists: " + type);
        }
    }

    public static void start() {
        SERVICE_MANAGER.startAsync();
    }

    public static void stop() {
        SERVICE_MANAGER.stopAsync();
    }
}
