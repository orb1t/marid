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

import javafx.beans.InvalidationListener;
import org.marid.ide.model.BeanData;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Collections.newSetFromMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanContext implements AutoCloseable {

    private final BeanData root;
    private final InvalidationListener listChangeListener = o -> reset();
    private final IdeValueConverterManager converters;
    private final ConcurrentHashMap<BeanData, BeanTypeInfo> typeInfoMap = new ConcurrentHashMap<>();
    private final Set<BeanData> processing = newSetFromMap(new ConcurrentHashMap<>());

    public BeanContext(BeanData root, ClassLoader classLoader) {
        this.root = root;
        this.converters = new IdeValueConverterManager(classLoader);
        this.root.children.addListener(listChangeListener);
    }

    Optional<BeanData> getBean(BeanData base, String name) {
        return base.stream().filter(b -> b.getName().equals(name)).findFirst();
    }

    public BeanTypeInfo get(BeanData beanData, Function<BeanData, BeanTypeInfo> typeInfoFunc) {
        return typeInfoMap.computeIfAbsent(beanData, d -> {
            if (processing.add(d)) {
                try {
                    return typeInfoFunc.apply(d);
                } finally {
                    processing.remove(d);
                }
            } else {
                return new EmptyBeanTypeInfo(new MaridBeanNotFoundException(beanData.getName()));
            }
        });
    }

    public IdeValueConverterManager getConverters() {
        return converters;
    }

    private void reset() {
        processing.clear();
        typeInfoMap.clear();
    }

    ClassLoader getClassLoader() {
        return converters.getClassLoader();
    }

    @Override
    public void close() {
        typeInfoMap.clear();
        root.children.removeListener(listChangeListener);
    }
}
