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
import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTableView<BeanData> {

    @Autowired
    public BeanTable(ProjectProfile profile, SpecialActions specialActions) {
        super(profile.getBeanFile().beans, specialActions);
        setEditable(true);
    }

    @Order(1)
    @Autowired
    private void nameColumn() {
        final TableColumn<BeanData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setMinWidth(150);
        column.setPrefWidth(200);
        column.setMaxWidth(700);
        column.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(column);
    }

    @Order(2)
    @Autowired
    private void factoryColumn() {
        final TableColumn<BeanData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Factory"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> param.getValue().factory);
        column.setCellFactory(param -> {
            final TextFieldTableCell<BeanData, String> cell = new TextFieldTableCell<>();
            cell.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                final String item = cell.getItem();
                if (item == null) {
                    return null;
                } else if (item.contains(".")) {
                    return FontIcons.glyphIcon("F_CUBE", 16);
                } else {
                    return FontIcons.glyphIcon("F_TREE", 16);
                }
            }, cell.itemProperty()));
            return cell;
        });
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void producerColumn() {
        final TableColumn<BeanData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Producer"));
        column.setMinWidth(200);
        column.setPrefWidth(250);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> param.getValue().getProducer().signature);
        getColumns().add(column);
    }

    @Autowired
    private void initAdd(SpecialAction addAction, BeanEditorContext context) {
        final FxAction action = new FxAction("add", "add", "add")
                .setSpecialAction(addAction)
                .bindText("Add a bean")
                .setIcon("D_SERVER_PLUS")
                .setDisabled(false);
        final InvalidationListener listener = o -> {
            final FxAction[] childen = context.discoveredBeans.stream()
                    .map(bean -> new FxAction("bean", bean.literal.group)
                            .setIcon(bean.literal.icon)
                            .bindText(ls("%s: %s", ls(bean.literal.name), ls(bean.literal.description))))
                    .toArray(FxAction[]::new);
            action.setChildren(childen);
        };
        context.discoveredBeans.addListener(listener);
        listener.invalidated(null);
        actions.add(data -> action);
    }
}
