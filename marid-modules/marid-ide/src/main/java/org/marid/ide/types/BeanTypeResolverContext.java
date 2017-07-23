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
import javafx.collections.WeakListChangeListener;
import org.marid.function.Suppliers;
import org.marid.ide.model.BeanData;
import org.marid.runtime.exception.MaridBeanNotFoundException;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTypeResolverContext {

    final IdeValueConverterManager converters;
    final TreeMap<String, BeanData> beanMap;
    final LinkedHashSet<String> processing = new LinkedHashSet<>();
    final Map<String, BeanTypeInfo> typeInfoMap = new HashMap<>();

    public BeanTypeResolverContext(ObservableList<BeanData> beans, ClassLoader classLoader) {
        converters = new IdeValueConverterManager(classLoader);
        beanMap = beans.stream().collect(toMap(BeanData::getName, identity(), (d1, d2) -> d2, TreeMap::new));
        beans.addListener(new WeakListChangeListener<BeanData>(this::onBeanChange));
    }

    public BeanData getBean(String name) {
        return Suppliers.get(beanMap, name, MaridBeanNotFoundException::new);
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
                if (!IntStream.range(0, list.size())
                        .map(i -> list.size() - i - 1)
                        .mapToObj(list::get)
                        .filter(d -> d.getName().equals(data.getName()))
                        .peek(e -> beanMap.put(e.getName(), e))
                        .peek(e -> reset(e.getName()))
                        .findFirst()
                        .isPresent()) {
                    beanMap.remove(data.getName());
                    reset(data.getName());
                }
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
}
