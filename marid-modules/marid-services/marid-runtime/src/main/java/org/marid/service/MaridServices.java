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

import com.google.common.util.concurrent.ServiceManager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static java.util.ServiceLoader.load;
import static org.marid.methods.LogMethods.severe;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridServices {

    private static final Logger LOG = Logger.getLogger(MaridServices.class.getName());
    private static final Set<MaridService> SERVICES = new LinkedHashSet<>();
    public static final ServiceManager SERVICE_MANAGER;

    static {
        try {
            for (final MaridServiceProvider provider : load(MaridServiceProvider.class)) {
                try {
                    SERVICES.addAll(provider.getServices());
                } catch (Exception x) {
                    warning(LOG, "Unable to get services from {0}", x, provider);
                }
            }
        } catch (Exception x) {
            severe(LOG, "Unable to load services", x);
        }
        SERVICE_MANAGER = new ServiceManager(SERVICES);
    }

    public static Set<MaridService> getServices() {
        return Collections.unmodifiableSet(SERVICES);
    }

    public static MaridService getServiceById(String id) {
        for (final MaridService service : SERVICES) {
            if (id.equals(service.id())) {
                return service;
            }
        }
        return null;
    }

    public static MaridService getServiceByType(String type) {
        for (final MaridService service : SERVICES) {
            if (type.equals(service.type())) {
                return service;
            }
        }
        return null;
    }

    public static Set<MaridService> getServicesByType(String type) {
        final Set<MaridService> set = new LinkedHashSet<>();
        for (final MaridService service : SERVICES) {
            if (type.equals(service.type())) {
                set.add(service);
            }
        }
        return set;
    }

    public static <T> Future<T> send(String type, Object message) {
        for (final MaridService service : SERVICES) {
            if (type.equals(service.type())) {
                return service.send(message);
            }
        }
        throw new IllegalArgumentException("Service not exists: " + type);
    }
}
