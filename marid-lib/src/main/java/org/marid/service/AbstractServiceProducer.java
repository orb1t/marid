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

import org.marid.l10n.Localized.S;
import org.marid.service.xml.ServiceDescriptor;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractServiceProducer implements ServiceProducer {

    @Override
    public String getName() {
        return getServiceClass().getSimpleName();
    }

    @Override
    public String getVersion() {
        String version = getServiceClass().getPackage().getImplementationVersion();
        return version == null ? "DEV" : version;
    }

    @Override
    public String getDescription() {
        String description = getServiceClass().getPackage().getImplementationTitle();
        return description == null ? "" : S.l(description);
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Service newInstance(String id, String type, ServiceDescriptor descriptor) {
        Class<? extends Service> cl = getServiceClass();
        Constructor<? extends Service> c;
        try {
            c = cl.getConstructor(String.class, String.class, ServiceDescriptor.class);
            return c.newInstance(id, type, descriptor);
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }
}
