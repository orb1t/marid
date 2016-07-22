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

package org.marid.hmi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class HmiStagesList extends TableView<Entry<String, Stage>> {

    private final Map<Stage, BooleanProperty> visibleProps = new IdentityHashMap<>();

    public HmiStagesList() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        {
            final TableColumn<Entry<String, Stage>, String> column = new TableColumn<>(s("Bean"));
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
            column.setPrefWidth(150);
            column.setMaxWidth(300);
            getColumns().add(column);
        }
        {
            final TableColumn<Entry<String, Stage>, String> column = new TableColumn<>(s("Title"));
            column.setCellValueFactory(param -> param.getValue().getValue().titleProperty());
            column.setPrefWidth(250);
            column.setMaxWidth(550);
            getColumns().add(column);
        }
        {
            final TableColumn<Entry<String, Stage>, Boolean> column = new TableColumn<>(s("Visible"));
            column.setCellValueFactory(param -> visibleProps.computeIfAbsent(param.getValue().getValue(), stage -> {
                final BooleanProperty property = new SimpleBooleanProperty(stage.isShowing());
                final ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
                    if (newValue) {
                        stage.show();
                    } else {
                        stage.hide();
                    }
                };
                property.addListener(listener);
                stage.showingProperty().addListener((observable, oldValue, newValue) -> {
                    property.removeListener(listener);
                    property.setValue(newValue);
                    property.addListener(listener);
                });
                return property;
            }));
            column.setCellFactory(param -> new CheckBoxTableCell<>());
            column.setPrefWidth(100);
            column.setMaxWidth(150);
            getColumns().add(column);
        }
    }
}
