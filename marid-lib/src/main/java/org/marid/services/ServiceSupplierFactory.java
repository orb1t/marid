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

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServiceSupplierFactory {

    private static ServiceLoader<ServiceSupplier> loader;
    private static final Properties properties = new Properties();
    private static final ConcurrentHashMap<String, Service> SERVICE_MAP = new ConcurrentHashMap<>();

    public static Service getService(String type, Map<String, Object> params) {
        Service service = SERVICE_MAP.get(type);
        if (service == null) {
            synchronized (SERVICE_MAP) {
                service = SERVICE_MAP.get(type);
                if (service != null) {
                    return service;
                }
                if (properties.isEmpty()) {
                    properties.setProperty("groovy.minVersion", "1.0");
                    try (InputStream is = ServiceSupplierFactory.class.getResourceAsStream("/services.properties")) {
                        properties.load(is);
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
                String version = properties.getProperty(type + ".version");
                String minVersion = properties.getProperty(type + ".minVersion");
                String maxVersion = properties.getProperty(type + ".maxVersion");
                if (loader == null) {
                    loader = ServiceLoader.load(ServiceSupplier.class);
                }
                TreeMap<String, ServiceSupplier> suppliers = new TreeMap<>();
                for (ServiceSupplier serviceSupplier : loader) {
                    if (!type.equals(serviceSupplier.getType())) {
                        continue;
                    }
                    suppliers.put(serviceSupplier.getVersion(), serviceSupplier);
                }
                if (version != null) {
                    ServiceSupplier serviceSupplier = suppliers.get(version);
                    if (serviceSupplier != null) {
                        service = serviceSupplier.newService(params);
                    }
                }
                if (service == null) {
                    boolean includeInf = "true".equals(properties.getProperty(type + ".includeInf"));
                    boolean includeSup = "true".equals(properties.getProperty(type + ".includeSup"));
                    NavigableMap<String, ServiceSupplier> subMap;
                    if (minVersion != null && maxVersion != null) {
                        subMap = suppliers.subMap(minVersion, includeInf, maxVersion, includeSup);
                    } else if (minVersion != null) {
                        subMap = suppliers.tailMap(minVersion, includeInf);
                    } else if (maxVersion != null) {
                        subMap = suppliers.headMap(maxVersion, includeSup);
                    } else {
                        subMap = suppliers;
                    }
                    if (subMap.isEmpty()) {
                        service = null;
                    } else {
                        service = subMap.lastEntry().getValue().newService(params);
                    }
                }
                SERVICE_MAP.put(type, service);
            }
        }
        return service;
    }
}
