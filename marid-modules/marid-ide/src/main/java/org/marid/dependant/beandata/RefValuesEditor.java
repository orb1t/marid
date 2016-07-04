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

package org.marid.dependant.beandata;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.l10n.L10n;
import org.marid.spring.xml.data.RefValue;

import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class RefValuesEditor<T extends RefValue<T>> extends TableView<T> {

    public RefValuesEditor(ObservableList<T> list) {
        super(list);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(L10n.s("Name"));
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().name);
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(L10n.s("Type"));
            col.setEditable(false);
            col.setPrefWidth(250);
            col.setMaxWidth(520);
            col.setCellValueFactory(param -> param.getValue().type);
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(L10n.s("Reference"));
            col.setEditable(false);
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().ref);
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(L10n.s("Value"));
            col.setEditable(true);
            col.setPrefWidth(500);
            col.setMaxWidth(1500);
            col.setCellValueFactory(param -> param.getValue().value);
        }));
    }
}
