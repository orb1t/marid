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

import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import org.marid.annotation.MetaLiteral;
import org.marid.dependant.beaneditor.dao.LibraryBeanDao;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.dependant.beaneditor.model.LibraryMethod;
import org.marid.dependant.beaneditor.model.WildBean;
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMethodData;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.*;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTableActions {

    @Bean
    @Qualifier("beanTable")
    public Function<BeanData, FxAction> addRootBeans(SpecialAction addAction,
                                                     LibraryBeanDao dao,
                                                     ObjectFactory<BeanTable> table,
                                                     ProjectProfile profile) {
        return data -> {
            final FxAction action = new FxAction("add", "add", "add")
                    .setSpecialAction(addAction)
                    .bindText("Add a bean")
                    .setIcon("D_SERVER_PLUS");
            final Consumer<ProjectProfile> listener = p -> {
                final Function<LibraryBean, FxAction> function = bean -> new FxAction("bean", bean.literal.group)
                        .setIcon(bean.literal.icon)
                        .bindText(ls("%s%s%s", lo(bean.literal)))
                        .setEventHandler(event -> table.getObject().getItems().add(new BeanData(bean.bean)));
                final Map<String, List<FxAction>> grouped = dao.beans()
                        .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
                action.setChildren(children(grouped));
                action.setDisabled(action.getChildren().isEmpty());
            };
            action.anchors.add(listener);
            listener.accept(profile);
            profile.addOnUpdate(listener);
            return action;
        };
    }

    @Bean
    @Qualifier("beanTable")
    public Function<BeanData, FxAction> factoryBeans(SpecialAction addAction,
                                                     LibraryBeanDao dao,
                                                     ObjectFactory<BeanTable> table,
                                                     ProjectProfile profile) {
        return data -> {
            if (data == null) {
                return null;
            }
            final FxAction action = new FxAction("add", "add", "add")
                    .setSpecialAction(addAction)
                    .bindText("Add a factory bean")
                    .setIcon("D_SERVER_PLUS");
            final Consumer<ProjectProfile> listener = p -> {
                final Function<LibraryBean, FxAction> function = bean -> new FxAction("bean", bean.literal.group)
                        .setIcon(bean.literal.icon)
                        .bindText(ls("%s%s%s", lo(bean.literal)))
                        .setEventHandler(event -> {
                            final BeanData beanData = new BeanData(bean.bean);
                            beanData.factory.set(data.getName());
                            table.getObject().getItems().add(beanData);
                        });
                final Map<String, List<FxAction>> grouped = dao.beans(data)
                        .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
                action.setChildren(children(grouped));
                action.setDisabled(action.getChildren().isEmpty());
            };
            action.anchors.add(listener);
            listener.accept(profile);
            profile.addOnUpdate(listener);
            return action;
        };
    }

    @Qualifier("beanTable")
    @Bean
    public Function<BeanData, FxAction> initializerAdder(ProjectProfile profile,
                                                         LibraryBeanDao dao,
                                                         SpecialAction addAction) {
        return data -> {
            if (data == null) {
                return null;
            }
            final FxAction action = new FxAction("add", "add", "add")
                    .bindText("Add an initializer")
                    .setIcon("D_PLUS")
                    .setSpecialAction(addAction);
            final Consumer<ProjectProfile> listener = p -> {
                final Function<LibraryMethod, FxAction> function = m -> new FxAction("bean", m.literal.group)
                        .setIcon(m.literal.icon)
                        .bindText(MaridRuntimeUtils.toCanonical(m.method.signature))
                        .setEventHandler(event -> {
                            final BeanMethodData d = new BeanMethodData(data, m.method);
                            data.initializers.add(d);
                        });
                final Map<String, List<FxAction>> grouped = dao.initializers(data)
                        .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
                action.setChildren(children(grouped));
                action.setDisabled(action.getChildren().isEmpty());
            };
            action.anchors.add(listener);
            listener.accept(profile);
            profile.addOnUpdate(listener);
            return action;
        };
    }

    @Qualifier("beanTable")
    @Bean
    public Function<BeanData, FxAction> wildBeanAdder(ProjectProfile profile,
                                                      LibraryBeanDao dao,
                                                      SpecialAction addAction) {
        return data -> new FxAction("add", "add", "add")
                .setSpecialAction(addAction)
                .bindText("Add a wild bean")
                .setIcon("D_SERVER_NETWORK")
                .setEventHandler(event -> {
                    final TextInputDialog dialog = new TextInputDialog();
                    dialog.setHeaderText(m("Enter a class name:"));
                    dialog.setTitle(s("Wild bean"));
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.showAndWait().ifPresent(type -> {
                        final WildBean[] beans = dao.beans(type).map(WildBean::new).toArray(WildBean[]::new);
                        if (beans.length == 0) {
                            n(WARNING, "No beans found");
                        }
                        final ChoiceDialog<WildBean> d = new ChoiceDialog<>(beans[0], beans);
                        d.setTitle(s("Bean selector"));
                        d.setHeaderText(m("Select a bean: "));
                        d.initModality(Modality.APPLICATION_MODAL);
                        d.showAndWait().ifPresent(bean -> profile.getBeanFile().beans.add(new BeanData(bean.bean)));
                    });
                });
    }

    public static Object[] lo(MetaLiteral literal) {
        if (literal.description.isEmpty()) {
            return new Object[]{ls(literal.name), "", ""};
        } else {
            return new Object[]{ls(literal.name), ": ", ls(literal.description)};
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
