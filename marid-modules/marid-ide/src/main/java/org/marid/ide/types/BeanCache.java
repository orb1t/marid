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

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.marid.function.Suppliers;
import org.marid.ide.model.BeanData;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanCache implements AutoCloseable {

    private final ObservableList<BeanData> beanList;
    private final HashMap<String, BeanData> beanMap;

    final IdeValueConverterManager converters;
    final LinkedHashSet<String> processing = new LinkedHashSet<>();
    final Map<String, BeanTypeInfo> typeInfoMap = new HashMap<>();

    public BeanCache(ObservableList<BeanData> beans, ClassLoader classLoader) {
        beanList = beans;
        converters = new IdeValueConverterManager(classLoader);
        beanMap = beans.stream().collect(toMap(BeanData::getName, identity(), (d1, d2) -> d2, HashMap::new));
        beanList.addListener(this::onBeanChange);
    }

    public BeanData getBean(String name) {
        return Suppliers.get(beanMap, name, MaridBeanNotFoundException::new);
    }

    public boolean containsBean(String name) {
        return beanMap.containsKey(name);
    }

    public void reset(String name) {
        typeInfoMap.remove(name);
        processing.remove(name);
    }

    public void reset() {
        typeInfoMap.clear();
        processing.clear();
    }

    public ClassLoader getClassLoader() {
        return converters.getClassLoader();
    }

    private void onBeanChange(Change<? extends BeanData> change) {
        final List<? extends BeanData> list = change.getList();
        if (list.isEmpty()) {
            reset();
            return;
        }
        while (change.next()) {
            for (final BeanData data : change.getRemoved()) {
                beanMap.remove(data.getName());
                reset(data.getName());
            }
            for (final BeanData data : change.getAddedSubList()) {
                beanMap.put(data.getName(), data);
            }
            if (change.wasUpdated()) {
                IntStream.range(change.getFrom(), change.getTo())
                        .mapToObj(list::get)
                        .forEach(e -> reset(e.getName()));
            }
        }
    }

    @Override
    public void close() {
        beanList.removeListener(this::onBeanChange);
    }
}
