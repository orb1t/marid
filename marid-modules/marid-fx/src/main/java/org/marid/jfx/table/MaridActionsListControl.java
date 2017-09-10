/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.table;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActionType;
import org.marid.jfx.action.SpecialActions;

import javax.annotation.Resource;

public interface MaridActionsListControl<T> extends MaridActionsControl<T> {

    ObservableList<T> getItems();

    @Resource
    default void setSelectAndRemoveActions(SpecialActions specialActions) {
        if (getClass().isAnnotationPresent(DisableSelectAndRemoveActions.class)) {
            return;
        }

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.REMOVE))
                .bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()))
                .setEventHandler(event -> getItems().removeAll(getSelectionModel().getSelectedItems()))
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.SELECT_ALL))
                .setDisabled(getSelectionModel().getSelectionMode().equals(SelectionMode.SINGLE))
                .setEventHandler(event -> getSelectionModel().selectAll())
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.CLEAR_ALL))
                .setEventHandler(event -> getItems().clear())
        );
    }
}
