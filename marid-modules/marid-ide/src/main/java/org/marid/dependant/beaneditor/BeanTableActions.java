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

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.ide.model.BeanData;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTableActions {

    @Bean
    @Qualifier("beanTable")
    public Function<BeanData, FxAction> addRootBeans(SpecialAction addAction,
                                                     BeanEditorContext context,
                                                     ObjectFactory<BeanTable> table) {
        return data -> {
            final FxAction action = new FxAction("add", "add", "add")
                    .setSpecialAction(addAction)
                    .bindText("Add a bean")
                    .setIcon("D_SERVER_PLUS");
            final InvalidationListener listener = o -> {
                final Function<LibraryBean, FxAction> function = bean -> new FxAction("bean", bean.literal.group)
                        .setIcon(bean.literal.icon)
                        .bindText(ls("%s%s%s", libraryBeanObservables(bean)))
                        .setEventHandler(event -> table.getObject().getItems().add(new BeanData(bean.bean)));
                final Map<String, List<FxAction>> grouped = context.discoveredBeans.stream()
                        .filter(b -> !b.bean.factory.startsWith("@"))
                        .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
                action.setChildren(children(grouped));
                action.setDisabled(action.getChildren().isEmpty());
            };
            action.anchors.add(listener);
            context.discoveredBeans.addListener(new WeakInvalidationListener(listener));
            listener.invalidated(null);
            return action;
        };
    }

    @Bean
    @Qualifier("beanTable")
    public Function<BeanData, FxAction> factoryBeans(SpecialAction addAction,
                                                     BeanEditorContext context,
                                                     ObjectFactory<BeanTable> table) {
        return data -> {
            if (data == null) {
                return null;
            } else {
                final FxAction action = new FxAction("add", "add", "add")
                        .setSpecialAction(addAction)
                        .bindText("Add a factory bean")
                        .setIcon("D_SERVER_PLUS");
                final InvalidationListener listener = o -> {
                    final Function<LibraryBean, FxAction> function = bean -> new FxAction("bean", bean.literal.group)
                            .setIcon(bean.literal.icon)
                            .bindText(ls("%s%s%s", libraryBeanObservables(bean)))
                            .setEventHandler(event -> {
                                final BeanData beanData = new BeanData(bean.bean);
                                beanData.factory.set("@" + data.getName());
                                table.getObject().getItems().add(beanData);
                            });
                    final Map<String, List<FxAction>> grouped = context.discoveredBeans.stream()
                            .filter(b -> b.bean.factory.startsWith("@"))
                            .filter(b -> context.discoveredBeans.stream()
                                    .filter(e -> e.bean.name.equals(b.bean.factory.substring(1)))
                                    .anyMatch(e -> e.bean.factory.equals(data.getFactory()))
                            )
                            .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
                    action.setChildren(children(grouped));
                    action.setDisabled(action.getChildren().isEmpty());
                };
                action.anchors.add(listener);
                context.discoveredBeans.addListener(new WeakInvalidationListener(listener));
                listener.invalidated(null);
                return action;
            }
        };
    }

    private static Object[] libraryBeanObservables(LibraryBean bean) {
        if (bean.literal.description.isEmpty()) {
            return new Object[] {ls(bean.literal.name), "", ""};
        } else {
            return new Object[] {ls(bean.literal.name), ": ", ls(bean.literal.description)};
        }
    }

    private List<FxAction> children(Map<String, List<FxAction>> grouped) {
        switch (grouped.size()) {
            case 1:
                return grouped.values().stream().flatMap(Collection::stream).collect(toList());
            default:
                return grouped.entrySet().stream().map(e -> new FxAction("", "")
                        .bindText(e.getKey())
                        .setChildren(e.getValue())).collect(Collectors.toList());
        }
    }
}
