/*
 *
 */

package org.marid.dependant.beaneditor;

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

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.marid.ide.model.Annotations;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanParameterTable extends TableView<Parameter> {

    @Autowired
    public BeanParameterTable(BeanTable beanTable) {
        beanTable.getSelectionModel().selectedItemProperty().addListener(this::onSelect);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Autowired
    @Order(1)
    private void nameColumn() {
        final TableColumn<Parameter, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Parameter"));
        column.setMinWidth(100);
        column.setPrefWidth(200);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getNameAsString()));
        getColumns().add(column);
    }

    @Autowired
    @Order(2)
    private void typeColumn() {
        final TableColumn<Parameter, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Type"));
        column.setMinWidth(200);
        column.setPrefWidth(300);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().toString()));
        getColumns().add(column);
    }

    @Autowired
    @Order(3)
    private void valueColumn() {
        final TableColumn<Parameter, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Value"));
        column.setMinWidth(200);
        column.setPrefWidth(300);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> new SimpleStringProperty(Annotations.value(param.getValue())));
        getColumns().add(column);
    }

    @Autowired
    private void initRowFactory() {
        setRowFactory(param -> {
            final TableRow<Parameter> row = new TableRow<>();
            row.setAlignment(Pos.BASELINE_LEFT);
            return row;
        });
    }

    private void onSelect(Observable observable, MethodDeclaration oldValue, MethodDeclaration newValue) {
        if (newValue == null) {
            getItems().clear();
        } else {
            getItems().setAll(newValue.getParameters());
        }
    }
}
