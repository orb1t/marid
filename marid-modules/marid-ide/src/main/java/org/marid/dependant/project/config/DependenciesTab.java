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

package org.marid.dependant.project.config;

import com.google.common.collect.ImmutableSet;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.marid.jfx.Props;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependenciesTab extends BorderPane {

    public DependenciesTab(Model model) {
        final DependencyTable dependencyTable = new DependencyTable(model);
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
                .add("Use defaults", FontIcon.M_BOOKMARK, event -> useDefaultDependencies(dependencyTable.getItems()))
                .addSeparator()
                .add("Cut", FontIcon.M_CONTENT_CUT, event -> {}, itemSelectionTrigger)
                .add("Copy", FontIcon.M_CONTENT_COPY, event -> {}, itemSelectionTrigger)
                .add("Paste", FontIcon.M_CONTENT_PASTE, event -> {}, itemSelectionTrigger)
                .build(t -> setMargin(t,  new Insets(10, 0, 0, 0))));
    }

    public static void useDefaultDependencies(List<Dependency> dependencies) {
        final Set<String> artifacts = ImmutableSet.of("marid-db", "marid-proto", "marid-web");
        dependencies.removeIf(d -> "org.marid".equals(d.getGroupId()) && artifacts.contains(d.getArtifactId()));
        artifacts.forEach(artifact -> {
            final Dependency dependency = new Dependency();
            dependency.setGroupId("org.marid");
            dependency.setArtifactId(artifact);
            dependency.setVersion("${marid.runtime.version}");
            dependencies.add(dependency);
        });
    }

    private static class DependencyTable extends TableView<Dependency> {

        public DependencyTable(Model model) {
            super(FXCollections.observableList(model.getDependencies()));
            getColumns().add(idColumn());
            getColumns().add(groupIdColumn());
            getColumns().add(artifactIdColumn());
            getColumns().add(versionColumn());
            getColumns().add(classifierColumn());
            setEditable(true);
        }

        private TableColumn<Dependency, Integer> idColumn() {
            final TableColumn<Dependency, Integer> col = new TableColumn<>("#");
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getTableView().getItems().indexOf(param.getValue()) + 1));
            col.setEditable(false);
            col.setSortable(false);
            col.setStyle("-fx-alignment: center-right");
            return col;
        }

        private TableColumn<Dependency, String> groupIdColumn() {
            final TableColumn<Dependency, String> col = new TableColumn<>("groupId");
            col.setCellValueFactory(param -> Props.stringProperty(param.getValue(), "groupId"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(150);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Dependency, String> artifactIdColumn() {
            final TableColumn<Dependency, String> col = new TableColumn<>("artifactId");
            col.setCellValueFactory(param -> Props.stringProperty(param.getValue(), "artifactId"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(150);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Dependency, String> versionColumn() {
            final TableColumn<Dependency, String> col = new TableColumn<>("version");
            col.setCellValueFactory(param -> Props.stringProperty(param.getValue(), "version"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(200);
            col.setStyle("-fx-alignment: center-right");
            col.setSortable(false);
            return col;
        }

        private TableColumn<Dependency, String> classifierColumn() {
            final TableColumn<Dependency, String> col = new TableColumn<>("classifier");
            col.setCellValueFactory(param -> Props.stringProperty(param.getValue(), "classifier"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(150);
            col.setSortable(false);
            return col;
        }
    }
}
