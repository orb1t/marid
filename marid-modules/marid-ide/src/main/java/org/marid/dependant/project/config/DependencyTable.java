/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.project.config;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.maven.model.Dependency;

import static org.marid.jfx.props.Props.stringProp;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependencyTable extends TableView<Dependency> {

  public DependencyTable(ObservableList<Dependency> dependencies) {
    super(dependencies);
    setEditable(true);
    getColumns().add(idColumn());
    getColumns().add(groupIdColumn());
    getColumns().add(artifactIdColumn());
    getColumns().add(versionColumn());
  }

  private TableColumn<Dependency, Integer> idColumn() {
    final TableColumn<Dependency, Integer> col = new TableColumn<>("#");
    col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getTableView().getItems().indexOf(param.getValue()) + 1));
    col.setEditable(false);
    col.setStyle("-fx-alignment: center-right");
    return col;
  }

  private TableColumn<Dependency, String> groupIdColumn() {
    final TableColumn<Dependency, String> col = new TableColumn<>("groupId");
    col.setCellValueFactory(param -> stringProp(param.getValue()::getGroupId, param.getValue()::setGroupId));
    col.setCellFactory(param -> new TextFieldTableCell<>());
    col.setPrefWidth(150);
    col.setEditable(true);
    return col;
  }

  private TableColumn<Dependency, String> artifactIdColumn() {
    final TableColumn<Dependency, String> col = new TableColumn<>("artifactId");
    col.setCellValueFactory(param -> stringProp(param.getValue()::getArtifactId, param.getValue()::setArtifactId));
    col.setCellFactory(param -> new TextFieldTableCell<>());
    col.setPrefWidth(150);
    col.setEditable(true);
    return col;
  }

  private TableColumn<Dependency, String> versionColumn() {
    final TableColumn<Dependency, String> col = new TableColumn<>("version");
    col.setCellValueFactory(param -> stringProp(param.getValue()::getVersion, param.getValue()::setVersion));
    col.setCellFactory(param -> new TextFieldTableCell<>());
    col.setPrefWidth(200);
    col.setStyle("-fx-alignment: center-right");
    col.setSortable(false);
    col.setEditable(true);
    return col;
  }
}
