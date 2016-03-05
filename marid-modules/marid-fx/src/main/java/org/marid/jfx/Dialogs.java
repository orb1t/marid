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

package org.marid.jfx;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Dialogs {

    static <T> Dialog<T> dialog(Node parent, String title, Supplier<T> supplier, Consumer<Dialog> consumer) {
        final Dialog<T> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent.getScene().getWindow());
        dialog.setTitle(title);
        dialog.setResultConverter(b -> b.getButtonData() == CANCEL_CLOSE ? null : supplier.get());
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        consumer.accept(dialog);
        return dialog;
    }
}
