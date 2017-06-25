/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
