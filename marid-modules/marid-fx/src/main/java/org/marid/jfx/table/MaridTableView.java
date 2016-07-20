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
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MaridTableView<T> extends TableView<T> {

    public final BooleanBinding clearDisabled;
    public final BooleanBinding changeDisabled;

    public MaridTableView(@Nonnull ObservableList<T> list) {
        super(list);
        clearDisabled = Bindings.isEmpty(list);
        changeDisabled = Bindings.isEmpty(getSelectionModel().getSelectedItems());
    }

    public void onClear(ActionEvent event) {
        getItems().clear();
    }

    public void onDelete(ActionEvent event) {
        getItems().removeAll(new ArrayList<>(getSelectionModel().getSelectedItems()));
    }
}
