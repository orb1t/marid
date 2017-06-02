/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.spring.postprocessors;

import org.marid.misc.Builder;
import org.marid.spring.dependant.IdeClassFilter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeAppContext extends AnnotationConfigApplicationContext {

    public IdeAppContext() {
        super(Builder.build(new DefaultListableBeanFactory(), beanFactory -> {
            IdeAutowirePostProcessor.register(beanFactory);
            beanFactory.registerSingleton("ideClassFiltter", new IdeClassFilter());
            beanFactory.addBeanPostProcessor(new MaridCommonPostProcessor());
        }));
        setAllowBeanDefinitionOverriding(true);
        setAllowCircularReferences(false);
    }
}
