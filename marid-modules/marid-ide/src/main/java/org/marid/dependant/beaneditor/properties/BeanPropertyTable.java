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

package org.marid.dependant.beaneditor.properties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.dependant.beaneditor.beans.BeanTable;
import org.marid.dependant.beaneditor.model.BeanProperty;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanPropertyTable extends TableView<BeanProperty> {

    public BeanPropertyTable() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Autowired
    @Order(1)
    private void keyColumn() {
        final TableColumn<BeanProperty, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Property"));
        column.setMinWidth(150);
        column.setPrefWidth(250);
        column.setMaxWidth(500);
        column.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(column);
    }

    @Autowired
    @Order(2)
    private void valueColumn() {
        final TableColumn<BeanProperty, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Value"));
        column.setMinWidth(150);
        column.setPrefWidth(300);
        column.setMaxWidth(1000);
        column.setCellValueFactory(param -> param.getValue().value);
        getColumns().add(column);
    }

    @Autowired
    private void init(BeanTable beanTable) {
        beanTable.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> setItems(nV.properties));
    }
}
