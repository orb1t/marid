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

package org.marid.runtime.meta;

import org.marid.beans.meta.BeanInfo;
import org.marid.beans.meta.BeanIntrospector;
import org.marid.runtime.MaridContextInitializer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanIntrospectorImpl implements BeanIntrospector {
    @Override
    public BeanInfo[] getBeans(ClassLoader classLoader) {
        try (final GenericApplicationContext context = MaridContextInitializer.applicationContext(classLoader)) {
            final List<BeanInfo> beans = new ArrayList<>();
            for (final String beanName : context.getBeanDefinitionNames()) {
                final BeanDefinition beanDefinition = context.getBeanDefinition(beanName);
                final Set<String> dependsOn = beanDefinition.getDependsOn() != null
                        ? Stream.of(beanDefinition.getDependsOn()).collect(Collectors.toCollection(LinkedHashSet::new))
                        : Collections.emptySet();
                final String type = beanDefinition.getBeanClassName();
                final String description = beanDefinition.getDescription() == null ? "" : beanDefinition.getDescription();
                beans.add(new BeanInfo(type, beanName, description, dependsOn));
            }
            return beans.toArray(new BeanInfo[beans.size()]);
        }
    }
}
