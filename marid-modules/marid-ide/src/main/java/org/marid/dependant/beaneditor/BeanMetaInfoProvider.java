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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
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
        final ApplicationContext context = new ApplicationContext(classLoader);
        if (classLoader != null) {
            context.setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));
            context.load("classpath*:/META-INF/meta/beans.xml");
        }
        context.refresh();
        return new BeansMetaInfo(context);
    }

    public BeansMetaInfo profileMetaInfo() {
        final URLClassLoader classLoader = profile.getClassLoader();
        final ApplicationContext context = new ApplicationContext(classLoader);
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
        context.load(resources.toArray(new Resource[resources.size()]));
        context.refresh();
        return new BeansMetaInfo(context);
    }

    private class ApplicationContext extends GenericXmlApplicationContext {

        private ApplicationContext(ClassLoader classLoader) {
            setDisplayName(profile.getName());
            setAllowCircularReferences(true);
            setAllowBeanDefinitionOverriding(true);
            setValidating(false);
            setClassLoader(classLoader);
        }

        @Override
        public void refresh() throws BeansException, IllegalStateException {
            prepareRefresh();
            prepareBeanFactory(obtainFreshBeanFactory());
        }
    }

    public static class BeansMetaInfo {

        private final ApplicationContext context;

        private BeansMetaInfo(ApplicationContext context) {
            this.context = context;
        }

        public BeanDefinition getBeanDefinition(String name) {
            return context.getBeanFactory().getBeanDefinition(name);
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
