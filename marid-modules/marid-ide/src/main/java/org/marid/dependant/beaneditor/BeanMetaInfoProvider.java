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

package org.marid.dependant.beaneditor;

import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public BeansMetaInfo beans() {
        final URLClassLoader classLoader = profile.getClassLoader();
        if (classLoader == null) {
            return new BeansMetaInfo(null);
        } else {
            return new BeansMetaInfo(new ApplicationContext(classLoader));
        }
    }

    private class ApplicationContext extends ClassPathXmlApplicationContext {

        private ApplicationContext(ClassLoader classLoader) {
            setDisplayName(profile.getName());
            if (classLoader != null) {
                setConfigLocation("classpath*:/META-INF/meta/beans.xml");
            }
            setAllowCircularReferences(true);
            setAllowBeanDefinitionOverriding(true);
            setValidating(false);
            setClassLoader(classLoader);

            prepareRefresh();
            prepareBeanFactory(obtainFreshBeanFactory());
        }
    }

    public static class BeansMetaInfo {

        private final ApplicationContext context;

        private BeansMetaInfo(ApplicationContext context) {
            this.context = context;
        }

        public List<BeanDefinitionHolder> beans() {
            return beans(context.getBeanDefinitionNames());
        }

        public List<BeanDefinitionHolder> beans(ResolvableType resolvableType) {
            return beans(context.getBeanNamesForType(resolvableType));
        }

        private List<BeanDefinitionHolder> beans(String... names) {
            return Stream.of(names)
                    .map(name -> new BeanDefinitionHolder(context.getBeanFactory().getBeanDefinition(name), name))
                    .collect(Collectors.toList());
        }
    }
}
