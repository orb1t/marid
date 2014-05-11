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

package org.marid.bd;

import javax.xml.bind.JAXBContext;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.ServiceLoader.load;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface BdObjectProvider {

    JAXBContext JAXB_CONTEXT = getJaxbContext();

    Collection<Class<?>> getClasses();

    static JAXBContext getJaxbContext() {
        try {
            final Set<Class<?>> classes = new LinkedHashSet<>();
            for (final BdObjectProvider bdObjectProvider : load(BdObjectProvider.class)) {
                classes.addAll(bdObjectProvider.getClasses());
            }
            return JAXBContext.newInstance(classes.toArray(new Class<?>[classes.size()]));
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
