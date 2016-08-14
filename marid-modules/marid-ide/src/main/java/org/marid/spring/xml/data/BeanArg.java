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
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

import static java.util.stream.Stream.of;
import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "constructor-arg")
public class BeanArg extends RefValue<BeanArg> {

    @Override
    public Optional<? extends Type> getType(ProjectProfile profile) {
        final BeanData beanData = profile.getBeanFiles()
                .stream()
                .flatMap(f -> f.getValue().beans.stream())
                .filter(d -> d.beanArgs.stream().anyMatch(a -> a == this))
                .findAny()
                .orElse(null);
        if (beanData != null) {
            final Parameter parameter = beanData.getConstructor(profile)
                    .flatMap(e -> of(e.getParameters()).filter(p -> parameterName(p).equals(name.get())).findAny())
                    .orElse(null);
            if (parameter != null) {
                return Optional.of(parameter.getParameterizedType());
            }
        }
        if (type.isNotEmpty().get()) {
            return profile.getClass(type.get());
        } else {
            return Optional.empty();
        }
    }
}
