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

package org.marid.ide;

import org.marid.service.AbstractMaridService;
import org.marid.service.AbstractMaridServiceParameters;
import org.marid.service.MaridService;
import org.marid.service.MaridServiceProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov.
 */
public class TestMaridServiceProvider implements MaridServiceProvider {
    @Override
    public Set<Class<? extends MaridService>> getServices() {
        return new HashSet<>(Arrays.asList(
                ServiceA.class,
                ServiceB.class,
                ServiceC.class));
    }

    public static class ServiceA extends AbstractMaridService {

        public ServiceA(AbstractMaridServiceParameters params) {
            super(params);
        }
    }

    public static class ServiceB extends AbstractMaridService {

        public ServiceB(ServiceA serviceA, AbstractMaridServiceParameters params) {
            super(params);
        }
    }

    public static class ServiceC extends AbstractMaridService {

        public ServiceC(AbstractMaridServiceParameters params, ServiceA serviceA, ServiceB serviceB) {
            super(params);
        }
    }
}
