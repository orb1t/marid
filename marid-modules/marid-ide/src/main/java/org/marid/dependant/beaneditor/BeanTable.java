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

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.marid.ide.beans.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTableView<BeanData> {

    @Autowired
    public BeanTable(ProjectProfile profile, SpecialActions specialActions) {
        super(profile.getBeansFile().beans, specialActions);
        setEditable(true);
    }

    @Order(1)
    @Autowired
    private void nameColumn() {
        final TableColumn<BeanData, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Name"));
        column.setMinWidth(200);
        column.setPrefWidth(250);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(column);
    }

    @Order(2)
    @Autowired
    private void typeColumn() {
        final TableColumn<BeanData, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Type"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void temporaryColumn() {
        final TableColumn<BeanData, Boolean> column = new TableColumn<>("T");
        column.setMinWidth(28);
        column.setPrefWidth(32);
        column.setMaxWidth(38);
        column.setEditable(true);
        column.setCellValueFactory(param -> param.getValue().temporary);
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        getColumns().add(column);
    }
}
