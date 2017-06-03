/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.jfx.icons;

import javafx.scene.Node;
import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.icons.FontIcons;

import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
public interface IconFactory {

    static Node icon(Level level) {
        switch (level.intValue()) {
            case Integer.MAX_VALUE:
                return icon("D_SELECT_OFF", "red");
            case Integer.MIN_VALUE:
                return icon("D_ARROW_ALL", "green");
            case 1000:
                return icon("M_ERROR", "red");
            case 900:
                return icon("F_WARNING", "orange");
            case 800:
                return icon("F_INFO_CIRCLE", "blue");
            case 700:
                return icon("M_CONTROL_POINT", "green");
            case 500:
                return icon("D_BATTERY_60", "green");
            case 400:
                return icon("D_BATTERY_80", "green");
            case 300:
                return icon("D_BATTERY_CHARGING_100", "green");
            default:
                return icon("D_BATTERY_UNKNOWN", "gray");
        }
    }

    static Node icon(@PropertyKey(resourceBundle = "fonts.meta") String icon, String color) {
        final Node glyphIcon = FontIcons.glyphIcon(icon, 16);
        glyphIcon.setStyle("-fx-fill: " + color);
        return glyphIcon;
    }
}
