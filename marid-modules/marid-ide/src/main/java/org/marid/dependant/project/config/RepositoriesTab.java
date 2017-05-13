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

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.marid.jfx.props.Props;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Qualifier("projectConf")
@Order(3)
public class RepositoriesTab extends BorderPane {

    public RepositoriesTab(Model model) {
        setId("Repositories");
        final RepositoryTable repositoryTable = new RepositoryTable(model);
        setCenter(repositoryTable);
        final Consumer<Button> itemSelectionTrigger = b -> b.disableProperty().bind(repositoryTable
                .getSelectionModel()
                .selectedItemProperty()
                .isNull());
        setBottom(new ToolbarBuilder()
                .add("Add item", "M_ADD", event -> repositoryTable.getItems().add(new Repository()))
                .add("Remove item", "M_REMOVE", event -> {
                    final int index = repositoryTable.getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        repositoryTable.getItems().remove(index);
                    }
                }, itemSelectionTrigger)
                .addSeparator()
                .add("Clear all items", "M_CLEAR_ALL",
                        event -> repositoryTable.getItems().clear(),
                        b -> b.disableProperty().bind(Bindings.size(repositoryTable.getItems()).isEqualTo(0)))
                .addSeparator()
                .add("Cut", "M_CONTENT_CUT", event -> {}, itemSelectionTrigger)
                .add("Copy", "M_CONTENT_COPY", event -> {}, itemSelectionTrigger)
                .add("Paste", "M_CONTENT_PASTE", event -> {}, itemSelectionTrigger)
                .build(t -> setMargin(t,  new Insets(10, 0, 0, 0))));
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
            col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getId, param.getValue()::setId));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(40);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> nameColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("Name");
            col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getName, param.getValue()::setName));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(70);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> urlColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("URL");
            col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getUrl, param.getValue()::setUrl));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setPrefWidth(150);
            col.setSortable(false);
            return col;
        }

        private TableColumn<Repository, String> layoutColumn() {
            final TableColumn<Repository, String> col = new TableColumn<>("Layout");
            col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getLayout, param.getValue()::setLayout));
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
            return Props.boolProp(() -> "true".equals(f.apply(repository).getEnabled()), e -> f.apply(repository).setEnabled(e ? "true" : null));
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
