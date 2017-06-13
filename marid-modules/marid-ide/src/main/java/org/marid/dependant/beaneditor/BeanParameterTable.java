/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.dependant.beaneditor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import javafx.beans.Observable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.ide.model.Annotations;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.beans.ConstantValue;
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
        column.setCellValueFactory(param -> ConstantValue.value(param.getValue().getNameAsString()));
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
        column.setCellValueFactory(param -> ConstantValue.value(param.getValue().getType().toString()));
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
        column.setCellValueFactory(param -> ConstantValue.value(Annotations.value(param.getValue())));
        getColumns().add(column);
    }

    private void onSelect(Observable observable, MethodDeclaration oldValue, MethodDeclaration newValue) {
        if (newValue == null) {
            getItems().clear();
        } else {
            getItems().setAll(newValue.getParameters());
        }
    }
}
