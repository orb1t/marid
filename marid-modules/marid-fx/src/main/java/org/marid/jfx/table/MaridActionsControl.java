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

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActionType;
import org.marid.jfx.action.SpecialActions;

import java.util.List;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridActionsControl<T> {

    ObservableList<Function<T, FxAction>> actions();

    MultipleSelectionModel<T> getSelectionModel();

    List<Observable> observables();

    List<Runnable> onConstructListeners();

    List<Runnable> onDestroyListeners();

    void remove(List<? extends T> list);

    void clearAll();

    default void installActions(SpecialActions specialActions) {
        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.REMOVE))
                .bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()))
                .setEventHandler(event -> remove(getSelectionModel().getSelectedItems()))
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.SELECT_ALL))
                .setDisabled(getSelectionModel().getSelectionMode().equals(SelectionMode.SINGLE))
                .setEventHandler(event -> getSelectionModel().selectAll())
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.CLEAR_ALL))
                .setEventHandler(event -> clearAll())
        );
    }
}
