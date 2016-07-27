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

package org.marid.dependant.beaneditor.beans;

import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanMetaInfoProvider {

    private final ProjectProfile profile;

    @Autowired
    public BeanMetaInfoProvider(ProjectProfile profile) {
        this.profile = profile;
    }

    public Map<String, BeanDefinition> beans() {
        final URLClassLoader classLoader = profile.getClassLoader();
        if (classLoader == null) {
            return Collections.emptyMap();
        } else {
            try (final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext()) {
                context.setDisplayName(profile.getName());
                context.setConfigLocation("classpath*:/META-INF/meta/beans.xml");
                context.setAllowBeanDefinitionOverriding(true);
                context.setAllowCircularReferences(true);
                context.setValidating(false);
                context.setClassLoader(classLoader);
                context.refresh();
                final String[] beanDefinitionNames = context.getBeanDefinitionNames();
                final Map<String, BeanDefinition> set = new LinkedHashMap<>();
                for (final String beanDefinitionName : beanDefinitionNames) {
                    set.put(beanDefinitionName, context.getBeanFactory().getBeanDefinition(beanDefinitionName));
                }
                return set;
            }
        }
    }
}
