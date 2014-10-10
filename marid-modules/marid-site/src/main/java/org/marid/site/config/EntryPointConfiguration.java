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

package org.marid.site.config;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.marid.site.spring.EntryPointType;
import org.marid.site.spring.InjectedEntryPoint;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class EntryPointConfiguration {

    private final Map<String, AbstractEntryPoint> entryPointMap = new HashMap<>();

    @InjectedEntryPoint
    public EntryPointConfiguration(Set<AbstractEntryPoint> entryPoints) {
        for (final AbstractEntryPoint entryPoint : entryPoints) {
            final EntryPointType entryPointType = entryPoint.getClass().getAnnotation(EntryPointType.class);
            entryPointMap.put(entryPointType.value(), entryPoint);
        }
    }

    public AbstractEntryPoint entryPoint(String path) {
        return entryPointMap.get(path);
    }

    public void visitEntryPoints(Consumer<AbstractEntryPoint> consumer) {
        entryPointMap.values().forEach(consumer::accept);
    }

    public void configure(Application application) {
        for (final EntryPoint entryPoint : entryPointMap.values()) {
            final EntryPointType entryPointType = entryPoint.getClass().getAnnotation(EntryPointType.class);
            final Map<String, String> properties = new LinkedHashMap<>();
            for (final EntryPointType.Property property : entryPointType.properties()) {
                properties.put(property.key(), property.value());
            }
            application.addEntryPoint(entryPointType.value(), () -> entryPoint, properties);
        }
    }
}
