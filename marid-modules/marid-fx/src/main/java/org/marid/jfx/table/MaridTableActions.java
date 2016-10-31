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
import javafx.scene.control.TableView;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcon;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface MaridTableActions {

    static FxAction removeAction(TableView<?> tableView) {
        return new FxAction("itemActions", "itemActions", "Actions")
                .setText(s("Remove"))
                .setIcon(FontIcon.M_REMOVE)
                .bindDisabled(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()))
                .setEventHandler(event -> tableView.getItems().removeAll(tableView.getSelectionModel().getSelectedItems()));
    }

    static FxAction clearAction(TableView<?> tableView) {
        return new FxAction("itemsActions", "itemsActions", "Actions")
                .setText(s("Clear"))
                .setIcon(FontIcon.M_CLEAR_ALL)
                .bindDisabled(Bindings.isEmpty(tableView.getItems()))
                .setEventHandler(event -> tableView.getItems().clear());
    }
}
