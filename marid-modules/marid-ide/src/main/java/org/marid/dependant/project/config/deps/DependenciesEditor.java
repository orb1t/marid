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

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependenciesEditor extends BorderPane {

    public DependenciesEditor(String name, List<Dependency> dependencies) {
        setId(name);
        final DependencyTable dependencyTable = new DependencyTable(dependencies);
        setCenter(dependencyTable);
        setBottom(new ToolbarBuilder()
                .add("Add item", FontIcon.M_ADD, event -> dependencyTable.getItems().add(new Dependency()))
                .add("Remove item", FontIcon.M_REMOVE, dependencyTable::onDelete, dependencyTable.changeDisabled)
                .addSeparator()
                .add("Clear all items", FontIcon.M_CLEAR_ALL, dependencyTable::onClear, dependencyTable.clearDisabled)
                .addSeparator()
                .add("Marid dependency", FontIcon.M_STAR_HALF, event -> {
                    for (final Dependency dependency : dependencyTable.getSelectionModel().getSelectedItems()) {
                        dependency.setGroupId("org.marid");
                        dependency.setVersion("${marid.runtime.version}");
                        dependencyTable.refresh();
                    }
                }, dependencyTable.changeDisabled)
                .build(t -> setMargin(t,  new Insets(10, 0, 0, 0))));
    }
}
