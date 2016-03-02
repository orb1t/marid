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

package org.marid.ide.beaned.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.marid.jfx.ScrollPanes;
import org.marid.l10n.L10nSupport;

import java.util.Objects;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataValueEditorFactory implements L10nSupport {

    public Dialog newEditor(Node node, BeanContext beanContext, RefData refData) {
        final StringProperty refProperty = new SimpleStringProperty(refData.getRef());
        final StringProperty valueProperty = new SimpleStringProperty(refData.getValue());
        refProperty.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                valueProperty.set(null);
            }
        });
        valueProperty.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                refProperty.set(null);
            }
        });
        final Dialog<Runnable> dialog = new Dialog<>();
        dialog.initOwner(node.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(refData.getName());
        dialog.setResultConverter(p -> p.getButtonData() == CANCEL_CLOSE ? null : () -> {
            refData.valueProperty().set(refProperty.get());
            refData.valueProperty().set(valueProperty.get());
        });
        final DialogPane dialogPane = new DialogPane();
        dialog.setDialogPane(dialogPane);
        dialogPane.setPrefSize(800, 600);
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        final ComboBox<String> refs = new ComboBox<>(FXCollections.observableArrayList("a", "b"));
        refs.setEditable(true);
        refs.valueProperty().bindBidirectional(refProperty);
        final TextArea value = new TextArea();
        value.textProperty().bindBidirectional(valueProperty);
        final GridPane gridPane = new GridPane();
        dialogPane.setContent(gridPane);
        gridPane.addRow(0, new Label(s("Reference") + ":"), refs);
        gridPane.addRow(0, ScrollPanes.scrollPane(value));
        return dialog;
    }
}
