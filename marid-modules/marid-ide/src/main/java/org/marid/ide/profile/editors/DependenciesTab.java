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

package org.marid.ide.profile.editors;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.marid.ide.icons.IdeIcons;
import org.marid.jfx.Props;
import org.marid.util.Builder;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependenciesTab extends BorderPane {

    public DependenciesTab(Model model) {
        final DependencyTable dependencyTable = new DependencyTable(model);
        setCenter(dependencyTable);
        final ToolBar toolBar = new ToolBar(
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.ADD, 20)))
                        .$(Button::setTooltip, new Tooltip("Add item"))
                        .$(Button::setOnAction, event -> {
                            final Dependency dependency = new Dependency();
                            dependencyTable.getItems().add(dependency);
                        })
                        .build(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.REMOVE, 20)))
                        .$(Button::setTooltip, new Tooltip("Remove item"))
                        .$(Button::setOnAction, event -> {
                            final int index = dependencyTable.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                dependencyTable.getItems().remove(index);
                            }
                        })
                        .build(),
                new Separator(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.CLEAR_ALL, 20)))
                        .$(Button::setTooltip, new Tooltip("Clear all items"))
                        .$(Button::setOnAction, event -> dependencyTable.getItems().clear())
                        .build(),
                new Separator(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.BOOKMARK, 20)))
                        .$(Button::setTooltip, new Tooltip("Use defaults"))
                        .$(Button::setOnAction, event -> useDefaultDependencies(dependencyTable.getItems()))
                        .build(),
                new Separator(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.CONTENT_CUT, 20)))
                        .$(Button::setTooltip, new Tooltip("Cut"))
                        .build(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.CONTENT_COPY, 20)))
                        .$(Button::setTooltip, new Tooltip("Copy"))
                        .build(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.CONTENT_PASTE, 20)))
                        .$(Button::setTooltip, new Tooltip("Paste"))
                        .build()
        );
        setBottom(toolBar);
        setMargin(toolBar, new Insets(10, 0, 0, 0));
    }

    public static void useDefaultDependencies(List<Dependency> dependencies) {
        dependencies.clear();
        final String maridVersion = System.getProperty("implementation.version");
        Collections.addAll(dependencies,
                new Builder<>(new Dependency())
                        .$(Dependency::setGroupId, "org.marid")
                        .$(Dependency::setArtifactId, "marid-db")
                        .$(Dependency::setVersion, maridVersion)
                        .build(),
                new Builder<>(new Dependency())
                        .$(Dependency::setGroupId, "org.marid")
                        .$(Dependency::setArtifactId, "marid-proto")
                        .$(Dependency::setVersion, maridVersion)
                        .build(),
                new Builder<>(new Dependency())
                        .$(Dependency::setGroupId, "org.marid")
                        .$(Dependency::setArtifactId, "marid-web")
                        .$(Dependency::setVersion, maridVersion)
                        .build());
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
            col.setPrefWidth(150);
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
