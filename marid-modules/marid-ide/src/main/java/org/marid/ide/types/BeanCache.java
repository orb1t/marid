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
import javafx.collections.ObservableList;
import org.marid.ide.model.BeanData;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanCache implements AutoCloseable {

    private final ObservableList<BeanData> beanList;
    private final InvalidationListener listChangeListener = o -> reset();

    final IdeValueConverterManager converters;
    final LinkedHashSet<String> processing = new LinkedHashSet<>();
    final HashMap<String, BeanTypeInfo> typeInfoMap = new HashMap<>();

    public BeanCache(ObservableList<BeanData> beans, ClassLoader classLoader) {
        beanList = beans;
        converters = new IdeValueConverterManager(classLoader);
        beanList.addListener(listChangeListener);
    }

    public BeanData getBean(String name) {
        return beanList.parallelStream()
                .filter(b -> name.equals(b.getName()))
                .findAny()
                .orElseThrow(() -> new MaridBeanNotFoundException(name));
    }

    public boolean containsBean(String name) {
        return beanList.parallelStream().anyMatch(b -> name.equals(b.getName()));
    }

    public void reset() {
        typeInfoMap.clear();
        processing.clear();
    }

    public ClassLoader getClassLoader() {
        return converters.getClassLoader();
    }

    @Override
    public void close() {
        typeInfoMap.clear();
        beanList.removeListener(listChangeListener);
    }
}
