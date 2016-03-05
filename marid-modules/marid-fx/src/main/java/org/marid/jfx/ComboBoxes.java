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

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ComboBoxes {

    static <T> ComboBox<T> comboBox(ObservableList<T> list, StringProperty textProperty) {
        final ComboBox<T> comboBox = new ComboBox<>(list);
        comboBox.setEditable(true);
        comboBox.getEditor().textProperty().bindBidirectional(textProperty);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        return comboBox;
    }
}
