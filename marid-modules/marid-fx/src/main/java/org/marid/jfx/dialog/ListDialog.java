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

package org.marid.jfx.dialog;

import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import org.marid.jfx.panes.MaridScrollPane;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ListDialog<T> extends Dialog<T> {

    private final ListView<T> listView;

    public ListDialog(ObservableStringValue text, ObservableList<T> list) {
        titleProperty().bind(text);
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node node = listView = new ListView<>(list);
        getDialogPane().setContent(MaridScrollPane.createMaridScrollPane(node));
        setResultConverter(t -> t.getButtonData() == OK_DONE ? listView.getSelectionModel().getSelectedItem() : null);
    }

    public ListView<T> getListView() {
        return listView;
    }
}
