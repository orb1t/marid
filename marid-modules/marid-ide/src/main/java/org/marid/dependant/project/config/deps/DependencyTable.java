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

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.maven.model.Dependency;
import org.marid.jfx.props.Props;
import org.marid.jfx.table.MaridTableView;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependencyTable extends MaridTableView<Dependency> {

    public DependencyTable(List<Dependency> dependencies) {
        super(FXCollections.observableList(dependencies));
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
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getGroupId, param.getValue()::setGroupId));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setPrefWidth(150);
        col.setSortable(false);
        return col;
    }

    private TableColumn<Dependency, String> artifactIdColumn() {
        final TableColumn<Dependency, String> col = new TableColumn<>("artifactId");
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getArtifactId, param.getValue()::setArtifactId));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setPrefWidth(150);
        col.setSortable(false);
        return col;
    }

    private TableColumn<Dependency, String> versionColumn() {
        final TableColumn<Dependency, String> col = new TableColumn<>("version");
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getVersion, param.getValue()::setVersion));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setPrefWidth(200);
        col.setStyle("-fx-alignment: center-right");
        col.setSortable(false);
        return col;
    }

    private TableColumn<Dependency, String> classifierColumn() {
        final TableColumn<Dependency, String> col = new TableColumn<>("classifier");
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getClassifier, param.getValue()::setClassifier));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setPrefWidth(150);
        col.setSortable(false);
        return col;
    }
}
