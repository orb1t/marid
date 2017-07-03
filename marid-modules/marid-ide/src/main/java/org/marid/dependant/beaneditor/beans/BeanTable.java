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

package org.marid.dependant.beaneditor.beans;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.marid.dependant.beaneditor.model.BeanFactoryMethod;
import org.marid.dependant.beaneditor.model.BeanModelUpdater;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTableView<BeanFactoryMethod> {

    @Autowired
    public BeanTable(BeanModelUpdater updater) {
        super(updater.getBeans());
        setEditable(true);
    }

    @Order(1)
    @Autowired
    private void nameColumn() {
        final TableColumn<BeanFactoryMethod, String> column = new TableColumn<>();
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
        final TableColumn<BeanFactoryMethod, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Type"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void lazyColumn() {
        final TableColumn<BeanFactoryMethod, Boolean> column = new TableColumn<>("L");
        column.setMinWidth(28);
        column.setPrefWidth(32);
        column.setMaxWidth(38);
        column.setEditable(true);
        column.setCellValueFactory(param -> param.getValue().lazy);
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        getColumns().add(column);
    }

    @Order(4)
    @Autowired
    private void prototypeColumn() {
        final TableColumn<BeanFactoryMethod, Boolean> column = new TableColumn<>("P");
        column.setMinWidth(28);
        column.setPrefWidth(32);
        column.setMaxWidth(38);
        column.setCellValueFactory(param -> param.getValue().prototype);
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        getColumns().add(column);
    }

    @Autowired
    private void rowFactory(List<MethodActionSupplier> actions) {
        initialize(new Initializer().setTableActions(e -> actions.stream()
                .map(a -> a.apply(e))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        ));
    }
}
