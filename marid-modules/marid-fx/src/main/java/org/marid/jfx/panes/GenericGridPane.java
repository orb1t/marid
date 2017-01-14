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

package org.marid.jfx.panes;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.marid.jfx.control.MaridLabel;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.HPos.LEFT;
import static javafx.scene.layout.Priority.NEVER;
import static javafx.scene.layout.Priority.SOMETIMES;

/**
 * @author Dmitry Ovchinnikov
 */
public class GenericGridPane extends GridPane {

    public GenericGridPane() {
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, NEVER, LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, MAX_VALUE, SOMETIMES, LEFT, true));
        setVgap(10);
        setHgap(10);
    }

    protected int getNextRowIndex() {
        return getChildren().stream().mapToInt(c -> c instanceof Separator ? 2 : 1).sum() / 2;
    }

    public TextField addTextField(String text, Supplier<String> supplier, Consumer<String> consumer) {
        final TextField textField = new TextField(supplier.get());
        textField.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        final MaridLabel label = new MaridLabel().format("%s: ", text);
        addRow(getNextRowIndex(), label, textField);
        return textField;
    }

    public CheckBox addBooleanField(String text, Supplier<Boolean> supplier, Consumer<Boolean> consumer) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(supplier.get());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        final MaridLabel label = new MaridLabel().format("%s: ", text);
        addRow(getNextRowIndex(), label, checkBox);
        return checkBox;
    }

    public Spinner<Integer> addIntField(String text, Supplier<Integer> supplier, Consumer<Integer> consumer, int low, int high, int step) {
        final Spinner<Integer> spinner = new Spinner<>(low, high, supplier.get(), step);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        final MaridLabel label = new MaridLabel().format("%s: ", text);
        addRow(getNextRowIndex(), label, spinner);
        return spinner;
    }

    public <T extends Node> T addControl(String text, Supplier<T> nodeSupplier) {
        final MaridLabel label = new MaridLabel().format("%s: ", text);
        final T node = nodeSupplier.get();
        addRow(getNextRowIndex(), label, node);
        return node;
    }

    public Separator addSeparator() {
        final Separator separator = new Separator();
        separator.setPrefWidth(Double.MAX_VALUE);
        add(separator, 0, getNextRowIndex(), 2, 1);
        return separator;
    }
}
