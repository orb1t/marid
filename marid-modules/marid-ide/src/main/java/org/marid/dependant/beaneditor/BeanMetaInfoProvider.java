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

import org.apache.commons.lang3.tuple.Pair;
import org.marid.ide.project.ProjectProfile;
import org.marid.logging.LogSupport;
import org.marid.spring.xml.MaridBeanDefinitionSaver;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanMetaInfoProvider implements LogSupport {

    private final ProjectProfile profile;

    @Autowired
    public BeanMetaInfoProvider(ProjectProfile profile) {
        this.profile = profile;
    }

    public BeansMetaInfo metaInfo() {
        final URLClassLoader classLoader = profile.getClassLoader();
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        if (classLoader != null) {
            beanFactory.setBeanClassLoader(classLoader);
            final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
            reader.setValidating(false);
            reader.setBeanClassLoader(classLoader);
            reader.setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));
            reader.loadBeanDefinitions("classpath*:/META-INF/meta/beans.xml");
        }
        return new BeansMetaInfo(beanFactory);
    }

    public BeansMetaInfo profileMetaInfo() {
        final URLClassLoader classLoader = profile.getClassLoader();
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(classLoader);
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.setValidating(false);
        reader.setBeanClassLoader(classLoader);
        final List<Resource> resources = new ArrayList<>();
        for (final Pair<Path, BeanFile> pair : profile.getBeanFiles()) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                MaridBeanDefinitionSaver.write(os, pair.getRight());
                resources.add(new ByteArrayResource(os.toByteArray()));
            } catch (Exception x) {
                log(WARNING, "Unable to save {0}", x, pair.getKey());
            }
        }
        reader.loadBeanDefinitions(resources.toArray(new Resource[resources.size()]));
        return new BeansMetaInfo(beanFactory);
    }

    public static class BeansMetaInfo {

        private final DefaultListableBeanFactory beanFactory;

        private BeansMetaInfo(DefaultListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        public BeanDefinition getBeanDefinition(String name) {
            return beanFactory.getBeanDefinition(name);
        }

        public List<BeanDefinitionHolder> beans() {
            return beans(beanFactory.getBeanDefinitionNames());
        }

        public List<BeanDefinitionHolder> beans(ResolvableType resolvableType) {
            if (Object.class.equals(resolvableType.getRawClass())) {
                return Collections.emptyList();
            }
            return beans(beanFactory.getBeanNamesForType(resolvableType));
        }

        private List<BeanDefinitionHolder> beans(String... names) {
            return Stream.of(names)
                    .map(name -> new BeanDefinitionHolder(beanFactory.getBeanDefinition(name), name))
                    .collect(Collectors.toList());
        }
    }
}
