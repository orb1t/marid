package org.marid.spring.dependant;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import javax.annotation.Resource;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public abstract class DependantConfiguration<T> {

    protected T param;

    @SuppressWarnings("unchecked")
    @Resource
    private void init(ApplicationContext context) {
        final ResolvableType type = ResolvableType.forClass(DependantConfiguration.class, getClass());
        final ResolvableType generic = type.getGeneric(0);
        final Class<?> c = generic.getRawClass();
        param = (T) context.getBean(c);
    }
}
