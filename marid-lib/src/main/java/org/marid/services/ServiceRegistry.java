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

import org.marid.func.Predicate1;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServiceRegistry {

    private static final ConcurrentMap<String, Set<Service>> SERVICE_MAP = new ConcurrentSkipListMap<>();

    public static Map<String, Set<Service>> getServiceMap() {
        TreeMap<String, Set<Service>> map;
        synchronized (SERVICE_MAP) {
             map = new TreeMap<>(SERVICE_MAP);
        }
        for (Map.Entry<String, Set<Service>> e : map.entrySet()) {
            synchronized (e.getValue()) {
                e.setValue(new LinkedHashSet<>(e.getValue()));
            }
        }
        return map;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void registerService(Service service) {
        String type = service.getType();
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
            if (set == null) {
                SERVICE_MAP.put(type, set = new LinkedHashSet<>());
            }
        }
        synchronized (set) {
            set.add(service);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void unregisterService(Service service) {
        String type = service.getType();
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
        }
        if (set != null) {
            synchronized (set) {
                set.remove(service);
            }
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static Service get(String type) {
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
        }
        if (set != null) {
            synchronized (set) {
                if (set.isEmpty()) {
                    throw new NoSuchElementException(type);
                } else {
                    return set.iterator().next();
                }
            }
        } else {
            throw new NoSuchElementException(type);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static Service get(String type, Predicate1<Service> predicate) {
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
        }
        if (set != null) {
            synchronized (set) {
                for (Service service : set) {
                    if (predicate.check(service)) {
                        return service;
                    }
                }
            }
            throw new IllegalStateException("Service not found: " + type);
        } else {
            throw new NoSuchElementException(type);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static Service get(String type, String key, Object value) {
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
        }
        if (set != null) {
            synchronized (set) {
                for (Service service : set) {
                    Object v = service.get(key);
                    if (v != null && v.equals(value)) {
                        return service;
                    }
                }
            }
            throw new IllegalStateException("Service not found: " + type);
        } else {
            throw new NoSuchElementException(type);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static Service get(String type, Map<String, Object> params) {
        Set<Service> set;
        synchronized (SERVICE_MAP) {
            set = SERVICE_MAP.get(type);
        }
        if (set != null) {
            synchronized (set) {
                for (Service service : set) {
                    if (service.keySet().containsAll(params.keySet())) {
                        Map<String, Object> map = new HashMap<>();
                        for (String k : params.keySet()) {
                            map.put(k, service.get(k));
                        }
                        if (map.equals(params)) {
                            return service;
                        }
                    }
                }
            }
            throw new IllegalStateException("Service not found: " + type);
        } else {
            throw new NoSuchElementException(type);
        }
    }
}
