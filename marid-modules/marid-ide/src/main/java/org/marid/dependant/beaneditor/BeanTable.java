/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.dependant.beaneditor;

import com.github.javaparser.ast.body.MethodDeclaration;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.marid.ide.model.Annotations.isLazy;
import static org.marid.ide.model.Annotations.isPrototype;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends TableView<MethodDeclaration> {

    @Autowired
    public BeanTable(BeanEditorUpdater beanEditorUpdater) {
        super(beanEditorUpdater.getBeans());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Order(1)
    @Autowired
    private void nameColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Name"));
        column.setMinWidth(200);
        column.setPrefWidth(250);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getNameAsString()));
        getColumns().add(column);
    }

    @Order(2)
    @Autowired
    private void typeColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Type"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(800);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().toString()));
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void lazyColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>("L");
        column.setMinWidth(20);
        column.setPrefWidth(24);
        column.setMaxWidth(28);
        column.setCellValueFactory(param -> new SimpleStringProperty(isLazy(param.getValue()) ? "\u25CF" : "\u25CB"));
        getColumns().add(column);
    }

    @Order(4)
    @Autowired
    private void prototypeColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>("P");
        column.setMinWidth(20);
        column.setPrefWidth(24);
        column.setMaxWidth(28);
        column.setCellValueFactory(param -> new SimpleStringProperty(isPrototype(param.getValue()) ? "\u25CF" : "\u25CB"));
        getColumns().add(column);
    }

    @Autowired
    private void initRowFactory() {
        setRowFactory(param -> {
            final TableRow<MethodDeclaration> row = new TableRow<>();
            return row;
        });
    }
}
