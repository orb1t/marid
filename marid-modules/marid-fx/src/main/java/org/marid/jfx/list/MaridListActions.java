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

package org.marid.jfx.list;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ListView;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcon;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface MaridListActions {

    static FxAction removeAction(ListView<?> listView) {
        return new FxAction("itemActions", "itemActions", "Actions")
                .setText("Remove")
                .setEventHandler(event -> listView.getItems().removeAll(listView.getSelectionModel().getSelectedItems()))
                .bindDisabled(Bindings.isEmpty(listView.getSelectionModel().getSelectedItems()))
                .setIcon(FontIcon.M_REMOVE);
    }

    static FxAction clearAction(ListView<?> listView) {
        return new FxAction("itemsActions", "itemsActions", "Actions")
                .setText("Clear")
                .setEventHandler(event -> listView.getItems().clear())
                .bindDisabled(Bindings.isEmpty(listView.getItems()))
                .setIcon(FontIcon.M_CLEAR_ALL);
    }
}
