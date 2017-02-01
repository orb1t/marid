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

package org.marid.jfx.controls;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import org.marid.jfx.action.FxAction;

import javax.annotation.Resource;
import java.util.function.Predicate;

import static javafx.scene.input.Clipboard.getSystemClipboard;

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

    public ObservableList<T> getSourceItems() {
        return getItems();
    }

    protected boolean removeEnabled(ObservableList<T> items) {
        return true;
    }

    protected boolean clearAllEnabled() {
        return true;
    }

    protected boolean copyEnabled(ObservableList<T> items) {
        return false;
    }

    protected boolean pasteEnabled(Clipboard clipboard) {
        return false;
    }

    protected void paste(Clipboard clipboard) {
    }

    protected void copy(ObservableList<T> items) {
    }

    private <E extends Observable> BooleanBinding binding(Predicate<E> predicate, E items) {
        return Bindings.createBooleanBinding(() -> predicate.test(items), items);
    }

    @Resource
    private void initRemove(FxAction removeAction) {
        removeAction.on(this, action -> {
            action.setEventHandler(event -> getSourceItems().removeAll(getSelectionModel().getSelectedItems()));
            action.bindDisabled(binding(v -> v.isEmpty() || !removeEnabled(v), getSelectionModel().getSelectedItems()));
        });
    }

    @Resource
    private void initClearAll(FxAction clearAllAction) {
        clearAllAction.on(this, a -> {
            a.setEventHandler(event -> getSourceItems().removeAll(getItems()));
            a.bindDisabled(binding(v -> v.isEmpty() || !removeEnabled(v), getItems()));
        });
    }

    @Resource
    private void initCut(FxAction cutAction) {
        cutAction.on(this, a -> {
            a.setEventHandler(event -> copy(getSelectionModel().getSelectedItems()));
            a.bindDisabled(binding(v -> v.isEmpty() || !removeEnabled(v) || !copyEnabled(v), getSelectionModel().getSelectedItems()));
        });
    }

    @Resource
    private void initPaste(FxAction pasteAction) {
        pasteAction.on(this, a -> {
            a.setEventHandler(event -> paste(getSystemClipboard()));
            a.bindDisabled(binding(v -> !pasteEnabled(getSystemClipboard()), getSelectionModel().selectedIndexProperty()));
        });
    }
}
