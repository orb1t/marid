/*
 *
 */

package org.marid.dependant.project.config.deps;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.maven.model.Dependency;
import org.marid.jfx.props.Props;

/**
 * @author Dmitry Ovchinnikov
 */
public class DependencyTable extends TableView<Dependency> {

    public DependencyTable(ObservableList<Dependency> dependencies) {
        super(dependencies.filtered(DependencyTable::filter));
        setEditable(true);
        getColumns().add(idColumn());
        getColumns().add(groupIdColumn());
        getColumns().add(artifactIdColumn());
        getColumns().add(versionColumn());
        getColumns().add(classifierColumn());
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
        col.setCellFactory(param -> {
            final ComboBoxTableCell<Dependency, String> cell = new ComboBoxTableCell<>("org.marid");
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(150);
        col.setSortable(false);
        col.setEditable(true);
        return col;
    }

    private TableColumn<Dependency, String> artifactIdColumn() {
        final TableColumn<Dependency, String> col = new TableColumn<>("artifactId");
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getArtifactId, param.getValue()::setArtifactId));
        col.setCellFactory(param -> {
            final ComboBoxTableCell<Dependency, String> cell = new ComboBoxTableCell<>(
                    "marid-db",
                    "marid-editors",
                    "marid-proto"
            );
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(150);
        col.setSortable(false);
        col.setEditable(true);
        return col;
    }

    private TableColumn<Dependency, String> versionColumn() {
        final TableColumn<Dependency, String> col = new TableColumn<>("version");
        col.setCellValueFactory(param -> Props.stringProp(param.getValue()::getVersion, param.getValue()::setVersion));
        col.setCellFactory(param -> {
            final ComboBoxTableCell<Dependency, String> cell = new ComboBoxTableCell<>("${marid.version}");
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(200);
        col.setStyle("-fx-alignment: center-right");
        col.setSortable(false);
        col.setEditable(true);
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

    private static boolean filter(Dependency dependency) {
        if (!"org.marid".equals(dependency.getGroupId())) {
            return true;
        }
        switch (dependency.getArtifactId()) {
            case "marid-runtime":
                return false;
        }
        return true;
    }
}
