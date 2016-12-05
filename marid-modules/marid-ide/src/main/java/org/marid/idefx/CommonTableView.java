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

package org.marid.idefx;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.marid.jfx.action.FxAction;

import javax.annotation.Resource;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class CommonTableView<T> extends TableView<T> {

    public CommonTableView() {
    }

    public CommonTableView(ObservableList<T> items) {
        super(items);
    }

    protected boolean removeEnabled(ObservableList<T> items) {
        return true;
    }

    protected boolean clearAllEnabled() {
        return true;
    }

    protected ObservableList<T> getSourceItems() {
        return getItems();
    }

    @Resource
    private void initRemove(FxAction removeAction) {
        removeAction.on(this, action -> {
            action.setEventHandler(event -> {
                if (removeEnabled(getSelectionModel().getSelectedItems())) {
                    getSourceItems().removeAll(getSelectionModel().getSelectedItems());
                }
            });
            action.bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()));
        });
    }

    @Resource
    private void initClearAllAction(FxAction clearAllAction) {
        clearAllAction.on(this, action -> {
            action.setEventHandler(event -> {
                if (clearAllEnabled()) {
                    getSourceItems().removeAll(getItems());
                }
            });
            action.bindDisabled(Bindings.isEmpty(getItems()));
        });
    }
}
