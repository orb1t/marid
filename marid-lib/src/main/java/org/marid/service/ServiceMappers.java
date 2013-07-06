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

import java.util.*;

import static java.util.ServiceLoader.load;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServiceMappers {

    private static final ServiceLoader<ServiceMapper> LOADER = load(ServiceMapper.class);

    public static ServiceMapper getServiceMapper() {
        Iterator<ServiceMapper> it = LOADER.iterator();
        return it.hasNext() ? it.next() : new ServiceMapper() {
            @Override
            public Service getService(String id) {
                return null;
            }

            @Override
            public Set<String> getServiceIds() {
                return Collections.emptySet();
            }

            @Override
            public Set<String> getServiceIds(String type) {
                return Collections.emptySet();
            }

            @Override
            public Service getService(String type, Map<String, String> serviceMap) {
                return null;
            }
        };
    }
}
