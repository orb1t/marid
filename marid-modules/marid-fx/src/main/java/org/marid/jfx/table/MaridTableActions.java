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
import org.marid.jfx.action.Action;
import org.marid.jfx.action.ActionConfigurer;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.FxActions;

import static org.marid.jfx.icons.FontIcon.M_CLEAR_ALL;
import static org.marid.jfx.icons.FontIcon.M_REMOVE;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridTableActions<T> extends FxActions {

    protected final TableView<T> tableView;

    public MaridTableActions(TableView<T> tableView) {
        this.tableView = tableView;
    }

    @Action(menu = "Items", group = "clear", tGroup = "clear", icon = M_CLEAR_ALL, name = "Clear", conf = ClearConfigurer.class)
    public void onClear(ActionEvent event) {
        tableView.getItems().clear();
    }

    @Action(menu = "Items", group = "clear", tGroup = "clear", icon = M_REMOVE, name = "Remove", conf = CommonItemConfigurer.class)
    public void onRemove(ActionEvent event) {
        tableView.getItems().removeAll(tableView.getSelectionModel().getSelectedItems());
    }

    public class ClearConfigurer implements ActionConfigurer {

        @Override
        public void configure(FxAction action) {
            action.bindDisabled(Bindings.isEmpty(tableView.getItems()));
        }
    }

    public class CommonItemConfigurer implements ActionConfigurer {

        @Override
        public void configure(FxAction action) {
            action.bindDisabled(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));
        }
    }
}
