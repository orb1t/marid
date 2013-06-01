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

package org.marid.services;

import java.util.*;
import java.util.logging.Logger;

import static org.marid.groovy.MaridGroovyMethods.severe;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServiceRegistry {

    private static final Logger LOG = Logger.getLogger(ServiceRegistry.class.getName());
    private static final TreeMap<String, ServiceSupplier> SUPPLIERS = new TreeMap<>();
    private static final TreeMap<String, TreeMap<String, Service>> SERVICES = new TreeMap<>();

    static {
        try {
            ServiceLoader<ServiceSupplier> loader = ServiceLoader.load(ServiceSupplier.class);
            synchronized (SUPPLIERS) {
                for (ServiceSupplier supplier : loader) {
                    SUPPLIERS.put(supplier.getType(), supplier);
                }
            }
        } catch (Exception x) {
            severe(LOG, "Unable to load services", x);
        }
    }

    public static List<ServiceSupplier> getServiceSuppliers() {
        synchronized (SUPPLIERS) {
            return new ArrayList<>(SUPPLIERS.values());
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static Service getDefaultService(String type) {
        TreeMap<String, Service> serviceMap;
        synchronized (SERVICES) {
            serviceMap = SERVICES.get(type);
            if (serviceMap == null) {
                SERVICES.put(type, serviceMap = new TreeMap<>());
            }
        }
        synchronized (serviceMap) {
            if (serviceMap.size() == 1) {
                return serviceMap.values().iterator().next();
            } else {
                Service result = null;
                for (Service service : serviceMap.values()) {
                    if (service.containsKey("default")) {
                        result = service;
                        break;
                    } else if (result == null) {
                        result = service;
                    }
                }
                return result;
            }
        }
    }

    public static void setDefault(String type, String name) {
        synchronized (SUPPLIERS) {
            for (Map.Entry<String, ServiceSupplier> e : SUPPLIERS.entrySet()) {
                if (type.equals(e.getKey())) {
                    e.getValue().setDefault(name.equals(e.getValue().getName()));
                }
            }
        }
    }
}
