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

package org.marid.dependant.beaneditor;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import org.marid.ide.model.BeanData;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.IdentityHashMap;

@Component
public class BeanTableListeners {

    private final IdentityHashMap<BeanData, ListChangeListener<BeanData>> changeListeners = new IdentityHashMap<>();

    @PreDestroy
    private void clean() {
        changeListeners.forEach((b, l) -> b.children.removeListener(l));
    }

    public TreeItem<BeanData> wrap(BeanData beanData) {
        final TreeItem<BeanData> item = new TreeItem<>(beanData);
        beanData.children.forEach(b -> item.getChildren().add(wrap(b)));
        final ListChangeListener<BeanData> listChangeListener = c -> {
            while (c.next()) {
                c.getRemoved().forEach(b -> {
                    item.getChildren().removeIf(i -> i.getValue() == b);
                    b.children.removeListener(changeListeners.getOrDefault(b, z -> {}));
                });
                c.getAddedSubList().forEach(b -> item.getChildren().add(wrap(b)));
            }
            item.getChildren().sort(Comparator.comparingInt(d -> c.getList().indexOf(d.getValue())));
        };
        changeListeners.put(beanData, listChangeListener);
        beanData.children.addListener(listChangeListener);
        item.getChildren().addListener((ListChangeListener<TreeItem<BeanData>>) c -> {
            while (c.next()) {
                c.getRemoved().forEach(i -> beanData.children.removeIf(d -> d == i.getValue()));
            }
        });
        return item;
    }
}
