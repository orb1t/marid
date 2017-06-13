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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.marid.ide.model.Annotations.isLazy;
import static org.marid.ide.model.Annotations.isPrototype;
import static org.marid.jfx.beans.ConstantValue.value;

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
        column.setCellValueFactory(param -> value(param.getValue().getNameAsString()));
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
        column.setCellValueFactory(param -> value(param.getValue().getType().toString()));
        getColumns().add(column);
    }

    @Order(3)
    @Autowired
    private void lazyColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>("L");
        column.setMinWidth(20);
        column.setPrefWidth(24);
        column.setMaxWidth(28);
        column.setCellValueFactory(param -> value(isLazy(param.getValue()) ? "\u25CF" : "\u25CB"));
        getColumns().add(column);
    }

    @Order(4)
    @Autowired
    private void prototypeColumn() {
        final TableColumn<MethodDeclaration, String> column = new TableColumn<>("P");
        column.setMinWidth(20);
        column.setPrefWidth(24);
        column.setMaxWidth(28);
        column.setCellValueFactory(param -> value(isPrototype(param.getValue()) ? "\u25CF" : "\u25CB"));
        getColumns().add(column);
    }
}
