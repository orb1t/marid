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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.marid.jfx.LocalizedStrings;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDialog<T> extends Dialog<T> {

    public MaridDialog(Window parent, ButtonType... buttonTypes) {
        initOwner(parent);
        initModality(Modality.WINDOW_MODAL);
        if (buttonTypes.length == 0) {
            getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CLOSE);
        } else {
            getDialogPane().getButtonTypes().addAll(buttonTypes);
        }
    }

    public MaridDialog(Node node, ButtonType... buttonTypes) {
        this(node.getScene().getWindow(), buttonTypes);
    }

    public MaridDialog(Modality modality) {
        initModality(modality);
    }

    public MaridDialog<T> content(Node content) {
        getDialogPane().setContent(content);
        return this;
    }

    public MaridDialog<T> title(ObservableValue<String> title) {
        titleProperty().bind(title);
        return this;
    }

    public MaridDialog<T> title(String title, Object... args) {
        titleProperty().bind(LocalizedStrings.ls(title, args));
        return this;
    }

    public MaridDialog<T> preferredSize(int width, int height) {
        getDialogPane().setPrefSize(width, height);
        return this;
    }

    public MaridDialog<T> buttonTypes(ButtonType... buttonTypes) {
        getDialogPane().getButtonTypes().addAll(buttonTypes);
        return this;
    }

    public MaridDialog<T> with(BiConsumer<MaridDialog<T>, DialogPane> consumer) {
        consumer.accept(this, getDialogPane());
        return this;
    }

    public <C extends Node> MaridDialog<T> with(Supplier<C> contentSupplier, BiConsumer<MaridDialog<T>, C> consumer) {
        final C content = contentSupplier.get();
        getDialogPane().setContent(content);
        consumer.accept(this, content);
        return this;
    }

    public MaridDialog<T> result(Supplier<T> okSupplier, Supplier<T> cancelSupplier) {
        setResultConverter(buttonType -> buttonType.getButtonData() == CANCEL_CLOSE
                ? cancelSupplier.get()
                : okSupplier.get());
        return this;
    }

    public MaridDialog<T> result(Supplier<T> okSupplier) {
        return result(okSupplier, () -> null);
    }

    public MaridDialog<T> on(Consumer<ButtonType> buttonTypeConsumer) {
        setResultConverter(buttonType -> {
            buttonTypeConsumer.accept(buttonType);
            return null;
        });
        return this;
    }

    public MaridDialog<T> resizable(boolean value) {
        setResizable(value);
        return this;
    }
}
