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

package org.marid.jfx.table;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.FxActions;
import org.marid.jfx.icons.FontIcon;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridTableActions<T> extends FxActions {

    protected final TableView<T> table;

    public MaridTableActions(TableView<T> table) {
        this.table = table;
    }

    public FxAction clearAction() {
        return new FxAction("clear", "clear", "Items")
                .setEventHandler(event -> table.getItems().clear())
                .bindDisabled(Bindings.isEmpty(table.getItems()))
                .setText("Clear")
                .setIcon(FontIcon.M_CLEAR_ALL);
    }

    public FxAction removeAction() {
        return new FxAction("clear", "clear", "Items")
                .setEventHandler(event -> table.getItems().removeAll(table.getSelectionModel().getSelectedItems()))
                .bindDisabled(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()))
                .setText("Clear")
                .setIcon(FontIcon.M_REMOVE);
    }

    public void onRemove(ActionEvent event) {
        table.getItems().removeAll(table.getSelectionModel().getSelectedItems());
    }
}
