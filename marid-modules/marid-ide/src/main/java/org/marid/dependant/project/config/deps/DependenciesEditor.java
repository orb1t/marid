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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.marid.jfx.toolbar.ToolbarBuilder;

import java.util.List;

import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependenciesEditor extends BorderPane {

    public DependenciesEditor(String name, List<Dependency> dependencies) {
        setId(name);
        final ObservableList<Dependency> list = FXCollections.observableList(dependencies);
        final DependencyTable dependencyTable = new DependencyTable(list);
        final ObservableList<Dependency> selected = dependencyTable.getSelectionModel().getSelectedItems();
        setCenter(dependencyTable);
        setBottom(new ToolbarBuilder()
                .add("Add item", M_ADD, event -> list.add(new Dependency()))
                .add("Remove item", M_REMOVE, event -> list.removeAll(selected), Bindings.isEmpty(selected))
                .addSeparator()
                .add("Clear all items", M_CLEAR_ALL, event -> list.clear(), Bindings.isEmpty(list))
                .build(t -> setMargin(t,  new Insets(10, 0, 0, 0))));
    }
}
