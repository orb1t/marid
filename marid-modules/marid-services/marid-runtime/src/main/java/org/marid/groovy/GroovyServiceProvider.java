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

package org.marid.groovy;

import groovy.lang.GroovyCodeSource;
import org.marid.service.MaridService;
import org.marid.service.MaridServiceProvider;
import org.marid.util.Utils;

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.checkedCollection;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyServiceProvider implements MaridServiceProvider {

    private static final Logger LOG = Logger.getLogger(GroovyServiceProvider.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends MaridService> getServices() {
        final List<MaridService> services = new LinkedList<>();
        try {
            final ClassLoader cl = Utils.getClassLoader(getClass());
            final Enumeration<URL> e = cl.getResources("services.groovy");
            while (e.hasMoreElements()) {
                final URL url = e.nextElement();
                try {
                    final Object o = GroovyRuntime.SHELL.evaluate(new GroovyCodeSource(url));
                    if (o instanceof MaridService) {
                        services.add((MaridService) o);
                    } else if (o instanceof Collection) {
                        services.addAll(checkedCollection((Collection) o, MaridService.class));
                    } else if (o instanceof Map) {
                        services.addAll(checkedCollection(((Map) o).values(), MaridService.class));
                    } else {
                        throw new IllegalStateException("Invalid service: " + o);
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to load services from {0}", x, url);
                }
            }

        } catch (Exception x) {
            warning(LOG, "Unable to load services", x);
        }
        return services;
    }
}
