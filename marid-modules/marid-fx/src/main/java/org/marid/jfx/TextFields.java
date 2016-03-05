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
import javafx.scene.control.TextField;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TextFields {

    static TextField textField(StringProperty property) {
        final TextField textField = new TextField();
        textField.textProperty().bindBidirectional(property);
        textField.setMaxWidth(Double.MAX_VALUE);
        return textField;
    }
}
