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
import org.marid.jfx.beans.ConstantValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditor extends TableView<MethodDeclaration> {

    @Autowired
    public BeanEditor(BeanEditorUpdater beanEditorUpdater) {
        super(beanEditorUpdater.getMethods());
    }

    @Order(1)
    @Autowired
    public void column1() {
        final TableColumn<MethodDeclaration, String> col = new TableColumn<>();
        col.textProperty().bind(LocalizedStrings.ls("Name"));
        col.setMinWidth(100);
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> ConstantValue.value(param.getValue().getNameAsString()));
        getColumns().add(col);
    }
}
