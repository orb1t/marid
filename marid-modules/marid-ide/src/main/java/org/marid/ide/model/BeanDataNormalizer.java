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

package org.marid.ide.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
public interface BeanDataNormalizer {

    static void registerNormalizer(BeanFile file) {
        final Map<String, BeanData> map = file.beans.stream().collect(toMap(BeanData::getName, b -> b, (v1, v2) -> v2));
        final Function<String, String> generator = name -> {
            if (map.containsKey(name)) {
                name = name.replaceFirst("(_new)+$", "");
                while (map.containsKey(name)) {
                    name += "_new";
                }
            }
            return name;
        };
        final ChangeListener<String> nameChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> o, String oldName, String newName) {
                final BeanData bean = requireNonNull(map.get(oldName), oldName);
                bean.name.removeListener(this);
                try {
                    newName = generator.apply(newName);
                    bean.name.set(newName);
                    final Set<BeanData> toRemove = new HashSet<>();
                    for (final BeanData b : file.beans) {
                        if (b.getFactory().startsWith("@" + oldName)) {
                            if (newName == null) {
                                toRemove.add(b);
                                continue;
                            } else {
                                b.factory.set("@" + newName);
                            }
                        }
                        for (final BeanMethodArgData a : b.getProducer().args) {
                            if ("ref".equals(a.getType()) && a.getValue() != null && a.getValue().equals(oldName)) {
                                a.value.set(newName);
                            }
                        }
                        for (final BeanMethodData i : b.initializers) {
                            for (final BeanMethodArgData a : i.args) {
                                if ("ref".equals(a.getType()) && a.getValue() != null && a.getValue().equals(oldName)) {
                                    a.value.set(newName);
                                }
                            }
                        }
                    }
                    file.beans.removeAll(toRemove);
                } finally {
                    if (newName != null) {
                        bean.name.addListener(this);
                        map.remove(oldName);
                        map.put(newName, bean);
                    }
                }
            }
        };
        file.beans.addListener(new ListChangeListener<BeanData>() {
            @Override
            public void onChanged(Change<? extends BeanData> change) {
                change.getList().removeListener(this);
                try {
                    while (change.next()) {
                        for (final BeanData bean : change.getRemoved()) {
                            map.remove(bean.getName());
                            nameChangeListener.changed(bean.name, bean.getName(), null);
                        }
                        for (final BeanData bean : change.getAddedSubList()) {
                            final String name = generator.apply(bean.getName());
                            bean.name.set(name);
                            map.put(name, bean);
                            bean.name.addListener(nameChangeListener);
                        }
                    }
                } finally {
                    change.getList().addListener(this);
                }
            }
        });
    }
}
