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
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.beans.ConstantValue;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTable extends TableView<MethodDeclaration> {

    @Autowired
    public BeanEditorTable(BeanEditorUpdater beanEditorUpdater) {
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

    @Order(2)
    @Autowired
    public void column2() {
        final TableColumn<MethodDeclaration, Node> col = new TableColumn<>();
        col.textProperty().bind(LocalizedStrings.ls("Characteristics"));
        col.setMinWidth(200);
        col.setPrefWidth(350);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> {
            final MethodDeclaration method = param.getValue();
            final HBox box = new HBox(3);
            if (method.isAnnotationPresent(Singleton.class)) {
                box.getChildren().add(FontIcons.glyphIcon("D_WHITE_BALANCE_SUNNY"));
            }
            if (method.isAnnotationPresent("Startup")) {
                box.getChildren().add(FontIcons.glyphIcon("D_PLAY"));
            }
            return ConstantValue.value(box);
        });
        getColumns().add(col);
    }
}
