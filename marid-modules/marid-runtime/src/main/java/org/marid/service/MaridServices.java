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

import org.marid.methods.LogMethods;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.Runtime.getRuntime;
import static java.util.ServiceLoader.load;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridServices {

    private static final Logger LOG = Logger.getLogger(MaridServices.class.getName());
    private static final ServiceLoader<MaridServiceProvider> SERVICE_PROVIDERS = load(MaridServiceProvider.class);
    static final NavigableMap<Class<?>, Collection<MaridService>> SERVICES = new ConcurrentSkipListMap<>((a, b) -> {
        if (a == b) {
            return 0;
        } else if (a.isAssignableFrom(b)) {
            return -1;
        } else if (b.isAssignableFrom(a)) {
            return 1;
        } else {
            return a.getName().compareTo(b.getName());
        }
    });

    public static Collection<MaridService> getServices() {
        final List<MaridService> services = new LinkedList<>();
        SERVICES.values().forEach(services::addAll);
        return services;
    }

    public static <T extends MaridService> List<T> getServices(Class<T> type) {
        final List<T> services = new LinkedList<>();
        for (final Map.Entry<Class<?>, Collection<MaridService>> e : SERVICES.tailMap(type).entrySet()) {
            if (type.isAssignableFrom(e.getKey())) {
                e.getValue().forEach(s -> services.add(type.cast(s)));
            } else {
                break;
            }
        }
        return services;
    }

    public static void start() {
        final int threads = getRuntime().availableProcessors() > 1 ? getRuntime().availableProcessors() : 8;
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        try {
            SERVICES.values().forEach(l -> l.forEach(s -> executorService.execute(() -> {
                try {
                    s.start();
                } catch (Exception x) {
                    LogMethods.severe(LOG, "Unable to start {0}", x, s);
                }
            })));
        } finally {
            executorService.shutdown();
        }
    }

    public static void stop() {
        final int threads = getRuntime().availableProcessors() > 1 ? getRuntime().availableProcessors() : 8;
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        try {
            SERVICES.values().forEach(l -> l.forEach(s -> executorService.execute(() -> {
                try {
                    s.close();
                } catch (Exception x) {
                    LogMethods.severe(LOG, "Unable to close {0}", x, s);
                }
            })));
        } finally {
            executorService.shutdown();
        }
    }

    public static Set<Class<? extends MaridService>> serviceClasses() {
        final Set<Class<? extends MaridService>> classes = new LinkedHashSet<>();
        try {
            for (final MaridServiceProvider provider : SERVICE_PROVIDERS) {
                try {
                    classes.addAll(provider.getServices());
                } catch (Exception x) {
                    LogMethods.warning(LOG, "Unable to load services from {0}", x, provider);
                }
            }
        } catch (Exception x) {
            LogMethods.severe(LOG, "Unable to enumerate services", x);
        }
        return classes;
    }
}
