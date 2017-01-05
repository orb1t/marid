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

package org.marid.runtime;

import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBaseApplicationContext extends GenericXmlApplicationContext {

    public MaridBaseApplicationContext(ClassLoader classLoader) {
        addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.addBeanPostProcessor(new MaridBeanPostProcessor(beanFactory));
            beanFactory.addBeanPostProcessor(new CommonAnnotationBeanPostProcessor());
        });
        setClassLoader(classLoader);
        setAllowCircularReferences(false);
        setValidating(false);
        setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));
        load("classpath*:/META-INF/marid/**/*.xml");
    }

    public MaridBaseApplicationContext() {
        this(Thread.currentThread().getContextClassLoader());
    }
}
