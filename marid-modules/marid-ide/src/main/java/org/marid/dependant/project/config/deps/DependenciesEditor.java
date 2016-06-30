/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.dependant.project.config.deps;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependenciesEditor extends BorderPane {

    public DependenciesEditor(List<Dependency> dependencies) {
        final DependencyTable dependencyTable = new DependencyTable(dependencies);
        setCenter(dependencyTable);
        final Consumer<Button> itemSelectionTrigger = b -> b.disableProperty().bind(dependencyTable
                .getSelectionModel()
                .selectedItemProperty()
                .isNull());
        setBottom(new ToolbarBuilder()
                .add("Add item", FontIcon.M_ADD, event -> dependencyTable.getItems().add(new Dependency()))
                .add("Remove item", FontIcon.M_REMOVE, event -> {
                    final int index = dependencyTable.getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        dependencyTable.getItems().remove(index);
                    }
                }, itemSelectionTrigger)
                .addSeparator()
                .add("Clear all items", FontIcon.M_CLEAR_ALL,
                        event -> dependencyTable.getItems().clear(),
                        b -> b.disableProperty().bind(Bindings.size(dependencyTable.getItems()).isEqualTo(0)))
                .addSeparator()
                .add("Cut", FontIcon.M_CONTENT_CUT, event -> {}, itemSelectionTrigger)
                .add("Copy", FontIcon.M_CONTENT_COPY, event -> {}, itemSelectionTrigger)
                .add("Paste", FontIcon.M_CONTENT_PASTE, event -> {}, itemSelectionTrigger)
                .build(t -> setMargin(t,  new Insets(10, 0, 0, 0))));
    }
}
