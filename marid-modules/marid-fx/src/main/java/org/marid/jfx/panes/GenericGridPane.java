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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.marid.l10n.L10nSupport;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.HPos.LEFT;
import static javafx.scene.layout.Priority.NEVER;
import static javafx.scene.layout.Priority.SOMETIMES;
import static org.marid.jfx.Props.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class GenericGridPane extends GridPane implements L10nSupport {

    public GenericGridPane() {
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, NEVER, LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, MAX_VALUE, SOMETIMES, LEFT, true));
        setVgap(10);
        setHgap(10);
    }

    protected int getNextRowIndex() {
        return getChildren().stream().mapToInt(c -> c instanceof Separator ? 2 : 1).sum() / 2;
    }

    protected void addTextField(String text, StringProperty stringProperty) {
        final TextField textField = new TextField();
        textField.textProperty().bindBidirectional(stringProperty);
        final Label label = new Label(s(text) + ": ");
        addRow(getNextRowIndex(), label, textField);
    }

    protected void addTextField(String text, Object bean, String property) {
        final TextField textField = new TextField();
        textField.textProperty().bindBidirectional(stringProperty(bean, property));
        final Label label = new Label(s(text) + ": ");
        addRow(getNextRowIndex(), label, textField);
    }

    protected void addBooleanField(String text, Object bean, String property) {
        final CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(booleanProperty(bean, property));
        final Label label = new Label(s(text) + ": ");
        addRow(getNextRowIndex(), label, checkBox);
    }

    protected void addIntField(String text, Object bean, String property, int low, int high, int step) {
        final IntegerProperty p = integerProperty(bean, property);
        final Spinner<Integer> spinner = new Spinner<>(low, high, p.get(), step);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            p.set(newValue);
        });
        final Label label = new Label(s(text) + ": ");
        addRow(getNextRowIndex(), label, spinner);
    }

    protected void addSeparator() {
        final Separator separator = new Separator();
        separator.setPrefWidth(Double.MAX_VALUE);
        add(separator, 0, getNextRowIndex(), 2, 1);
    }
}
