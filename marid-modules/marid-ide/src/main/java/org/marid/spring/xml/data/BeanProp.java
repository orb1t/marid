/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring.xml.data;

import org.marid.ide.project.ProjectProfile;

import javax.xml.bind.annotation.XmlRootElement;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "property")
public class BeanProp extends RefValue<BeanProp> {

    @Override
    public Optional<? extends Type> getType(ProjectProfile profile) {
        final BeanData beanData = profile.getBeanFiles().values()
                .stream()
                .flatMap(f -> f.beans.stream())
                .filter(d -> d.properties.stream().anyMatch(a -> a == this))
                .findAny()
                .orElse(null);
        if (beanData != null) {
            final PropertyDescriptor descriptor = beanData.getPropertyDescriptors(profile)
                    .filter(d -> d.getName().equals(name.get()))
                    .findAny()
                    .orElse(null);
            if (descriptor != null) {
                return Optional.of(descriptor.getWriteMethod().getGenericParameterTypes()[0]);
            }
        }
        if (type.isNotEmpty().get()) {
            return profile.getClass(type.get());
        } else {
            return Optional.empty();
        }
    }
}
