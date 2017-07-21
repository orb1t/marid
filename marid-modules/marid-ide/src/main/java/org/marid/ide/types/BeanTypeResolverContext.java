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

package org.marid.ide.types;

import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTypeResolverContext {

    final IdeValueConverterManager converters;
    final Map<String, BeanData> beanMap;
    final LinkedHashSet<String> processing = new LinkedHashSet<>();
    final Map<String, BeanTypeInfo> typeInfoMap = new HashMap<>();

    public BeanTypeResolverContext(Iterable<BeanData> beans, ClassLoader classLoader) {
        converters = new IdeValueConverterManager(classLoader);
        beanMap = stream(beans.spliterator(), false).collect(toMap(BeanData::getName, identity()));
    }

    public BeanTypeResolverContext(ProjectProfile profile) {
        this(profile.getBeanFile().beans, profile.getClassLoader());
    }

    public BeanData getBean(String name) {
        return beanMap.get(name);
    }

    public void reset(String name) {
        typeInfoMap.remove(name);
    }

    ClassLoader getClassLoader() {
        return converters.getClassLoader();
    }
}
