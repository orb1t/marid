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

package org.marid.ide.settings.editors;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.marid.ide.settings.SettingsHolder;
import org.marid.l10n.L10nSupport;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.HPos.LEFT;
import static javafx.scene.layout.Priority.NEVER;
import static javafx.scene.layout.Priority.SOMETIMES;

/**
 * @author Dmitry Ovchinnikov
 */
public class CommonTab extends GridPane implements L10nSupport {

    public CommonTab(SettingsHolder holder) {
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, NEVER, LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, MAX_VALUE, SOMETIMES, LEFT, true));
        setVgap(10);
        setHgap(10);
        addTextField("Releases update policy by default", holder.releaseUpdatePolicy);
        addTextField("Snapshots update policy by default", holder.snapshotUpdatePolicy);
    }

    private void addTextField(String text, StringProperty stringProperty) {
        final TextField textField = new TextField();
        textField.textProperty().bindBidirectional(stringProperty);
        final Label label = new Label(s(text) + ": ");
        addRow(getChildren().size() / 2, label, textField);
    }
}
