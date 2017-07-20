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

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import org.marid.ide.model.BeanMemberData;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanArgsTable extends MaridTableView<BeanMemberData> {

    public BeanArgsTable(SpecialActions specialActions) {
        super(FXCollections.observableArrayList(), specialActions);
        setEditable(false);
    }

    @Order(1)
    @Autowired
    private void initNameColumn() {
        final TableColumn<BeanMemberData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setMinWidth(100);
        column.setPrefWidth(150);
        column.setPrefWidth(350);
        column.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(column);
    }

    @Order(2)
    @Autowired
    private void initTypeColumn() {
        final TableColumn<BeanMemberData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Type"));
        column.setMinWidth(100);
        column.setPrefWidth(150);
        column.setPrefWidth(350);
        column.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void initValueColumn() {
        final TableColumn<BeanMemberData, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Value"));
        column.setMinWidth(200);
        column.setPrefWidth(350);
        column.setPrefWidth(1000);
        column.setCellValueFactory(param -> param.getValue().value);
        getColumns().add(column);
    }
}
