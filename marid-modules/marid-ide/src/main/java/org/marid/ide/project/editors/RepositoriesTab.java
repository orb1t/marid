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

package org.marid.ide.project.editors;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.marid.ide.icons.IdeIcons;
import org.marid.util.Builder;

import java.util.function.Function;

import static org.marid.jfx.Props.booleanProperty;
import static org.marid.jfx.Props.stringProperty;

/**
 * @author Dmitry Ovchinnikov
 */
public class RepositoriesTab extends BorderPane {

    public RepositoriesTab(Model model) {
        final RepositoryTable repositoryTable = new RepositoryTable(model);
        setCenter(repositoryTable);
        final ToolBar toolBar = new ToolBar(
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.ADD, 20)))
                        .$(Button::setTooltip, new Tooltip("Add item"))
                        .$(Button::setOnAction, event -> {
                            final Repository repository = new Repository();
                            repositoryTable.getItems().add(repository);
                        })
                        .build(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.REMOVE, 20)))
                        .$(Button::setTooltip, new Tooltip("Remove item"))
                        .$(Button::setOnAction, event -> {
                            final int index = repositoryTable.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                repositoryTable.getItems().remove(index);
                            }
                        })
                        .build(),
                new Separator(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.CLEAR_ALL, 20)))
                        .$(Button::setTooltip, new Tooltip("Clear all items"))
                        .$(Button::setOnAction, event -> repositoryTable.getItems().clear())
                        .build(),
                new Separator(),
                new Builder<>(new Button(null, IdeIcons.glyphIcon(MaterialIcon.BOOKMARK, 20)))
                        .$(Button::setTooltip, new Tooltip("Use defaults"))
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
        toolBar.getItems().forEach(item -> item.setFocusTraversable(false));
    }

    private static class RepositoryTable extends TableView<Repository> {

        public RepositoryTable(Model model) {
            super(FXCollections.observableList(model.getRepositories()));
            getColumns().add(noColumn());
            getColumns().add(idColumn());
            getColumns().add(nameColumn());
            getColumns().add(urlColumn());
            getColumns().add(layoutColumn());
            getColumns().add(snapshotsColumn());
            getColumns().add(releasesColumn());
            setEditable(true);
        }

        private TableColumn<Repository, Integer> noColumn() {
            final TableColumn<Repository, Integer> col = new TableColumn<>("#");
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getTableView().getItems().indexOf(param.getValue()) + 1));
            col.setEditable(false);
            col.setSortable(false);
            col.setStyle("-fx-alignment: center-right");
            return col;
        }

        private TableColumn<Repository, String> idColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("Id");
            col.setCellValueFactory(param -> stringProperty(param.getValue(), "id"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(40);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> nameColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("Name");
            col.setCellValueFactory(param -> stringProperty(param.getValue(), "name"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(70);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> urlColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("URL");
            col.setCellValueFactory(param -> stringProperty(param.getValue(), "url"));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(150);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> layoutColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("Layout");
            col.setCellValueFactory(param -> stringProperty(param.getValue(), "layout"));
            col.setCellFactory(ComboBoxTableCell.forTableColumn("default", "legacy"));
            col.setPrefWidth(120);
            col.setSortable(false);
            return col;
        }

        private BooleanProperty policyEnabled(Repository repository, Function<Repository, RepositoryPolicy> f) {
            if (repository.getSnapshots() == null) {
                repository.setSnapshots(new RepositoryPolicy());
            }
            if (repository.getReleases() == null) {
                repository.setReleases(new RepositoryPolicy());
            }
            return booleanProperty(
                    () -> "true".equals(f.apply(repository).getEnabled()),
                    e -> f.apply(repository).setEnabled(e ? "true" : null));
        }

        private TableColumn<Repository, Boolean> snapshotsColumn() {
            final TableColumn<Repository, Boolean> col = new TableColumn<>("Snapshots");
            col.setCellValueFactory(param -> policyEnabled(param.getValue(), Repository::getSnapshots));
            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            col.setPrefWidth(100);
            col.setStyle("-fx-alignment: center");
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, Boolean> releasesColumn() {
            final TableColumn<Repository, Boolean> col = new TableColumn<>("Releases");
            col.setCellValueFactory(param -> policyEnabled(param.getValue(), Repository::getReleases));
            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            col.setPrefWidth(100);
            col.setStyle("-fx-alignment: center");
            col.setSortable(false);
            return col;
        }
    }
}
