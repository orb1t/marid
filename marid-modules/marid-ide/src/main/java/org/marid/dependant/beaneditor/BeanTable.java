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
import javafx.scene.control.TableColumn;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTableView<BeanData> {

    private final TableColumn<BeanData, String> nameColumn;
    private final TableColumn<BeanData, String> factoryColumn;
    private final TableColumn<BeanData, String> producerColumn;

    @Autowired
    public BeanTable(ProjectProfile profile) {
        super(profile.getBeanFile().beans);
        setEditable(true);

        nameColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Name"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(700);
            column.setCellValueFactory(param -> param.getValue().name);
            getColumns().add(column);
        });

        factoryColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Factory"));
            column.setMinWidth(300);
            column.setPrefWidth(350);
            column.setMaxWidth(800);
            column.setCellValueFactory(param -> param.getValue().factory);
            getColumns().add(column);
        });

        producerColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Producer"));
            column.setMinWidth(200);
            column.setPrefWidth(250);
            column.setMaxWidth(800);
            column.setCellValueFactory(param -> param.getValue().getProducer().signature);
            getColumns().add(column);
        });
    }

    @Autowired
    private void initAdd(SpecialAction addAction, BeanEditorContext context) {
        final FxAction action = new FxAction("add", "add", "add")
                .setSpecialAction(addAction)
                .bindText("Add a bean")
                .setIcon("D_SERVER_PLUS")
                .setDisabled(false);
        final InvalidationListener listener = o -> {
            final Function<LibraryBean, Object[]> textFunc = bean -> {
                if (bean.literal.description.isEmpty()) {
                    return new Object[] {ls(bean.literal.name), "", ""};
                } else {
                    return new Object[] {ls(bean.literal.name), ": ", ls(bean.literal.description)};
                }
            };
            final Function<LibraryBean, FxAction> function = bean -> new FxAction("bean", bean.literal.group)
                    .setIcon(bean.literal.icon)
                    .bindText(ls("%s%s%s", textFunc.apply(bean)))
                    .setEventHandler(event -> {
                        final BeanData beanData = new BeanData(bean.bean);
                        getItems().add(beanData);
                    });
            final Map<String, List<FxAction>> grouped = context.discoveredBeans.stream()
                    .collect(groupingBy(b -> b.literal.group, TreeMap::new, mapping(function, toList())));
            switch (grouped.size()) {
                case 1:
                    action.setChildren(grouped.values().stream().flatMap(Collection::stream).collect(toList()));
                    break;
                default:
                    action.setChildren(grouped.entrySet().stream().map(e -> {
                        final FxAction a = new FxAction("", "");
                        a.bindText(e.getKey());
                        a.setChildren(e.getValue());
                        return a;
                    }).collect(Collectors.toList()));
                    break;
            }
        };
        context.discoveredBeans.addListener(listener);
        listener.invalidated(null);
        actions.add(data -> action);
        observables.add(context.discoveredBeans);
    }
}
