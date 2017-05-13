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
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.marid.jfx.converter.MaridConverter;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.util.Dependencies;

import javax.annotation.PostConstruct;
import java.util.List;

import static javafx.beans.binding.Bindings.createBooleanBinding;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class DependenciesPane extends BorderPane {

    private final ObservableList<Dependency> dependencies;
    private final DependencyTable dependencyTable;
    private final ComboBox<Dependency> dependencyBox;

    public DependenciesPane(String name, List<Dependency> deps, List<Dependency> repositoryDeps) {
        setId(name);
        dependencies = FXCollections.observableList(deps);
        dependencyTable = new DependencyTable(dependencies);
        dependencyBox = new ComboBox<>(FXCollections.observableList(repositoryDeps));
        dependencyBox.setConverter(new MaridConverter<>(DependenciesPane::format));
        dependencyBox.setEditable(false);
        if (!dependencyBox.getItems().isEmpty()) {
            dependencyBox.getSelectionModel().select(dependencyBox.getItems().get(0));
        }
    }

    private static String format(Dependency dependency) {
        return String.format("%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    private boolean addDisabled() {
        return dependencyBox.getValue() == null
                || dependencies.stream().anyMatch(d -> Dependencies.equals(d, dependencyBox.getValue()));
    }

    @PostConstruct
    private void initTable() {
        setCenter(dependencyTable);
    }

    @PostConstruct
    private void initToolbar() {
        final ObservableList<Dependency> selected = dependencyTable.getSelectionModel().getSelectedItems();
        final BooleanBinding disabledAdd = createBooleanBinding(this::addDisabled,
                dependencyBox.valueProperty(),
                dependencies
        );
        setBottom(new ToolbarBuilder()
                .add(dependencyBox, b -> {})
                .addSeparator()
                .add("Add item", "M_ADD", event -> dependencies.add(dependencyBox.getValue()), disabledAdd)
                .add("Remove item", "M_REMOVE", event -> dependencies.removeAll(selected), Bindings.isEmpty(selected))
                .addSeparator()
                .add("Clear all items", "M_CLEAR_ALL", event -> dependencies.clear(), Bindings.isEmpty(dependencies))
                .build(t -> setMargin(t, new Insets(10, 0, 0, 0))));
    }
}
