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

package org.marid.ide.logging;

import de.jensd.fx.glyphs.GlyphIcon;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;

import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
public interface LogIconFactory {

    static GlyphIcon<?> icon(Level level) {
        switch (level.intValue()) {
            case Integer.MAX_VALUE:
                return icon(FontIcon.D_SELECT_OFF, "red");
            case Integer.MIN_VALUE:
                return icon(FontIcon.D_ARROW_ALL, "green");
            case 1000:
                return icon(FontIcon.M_ERROR, "red");
            case 900:
                return icon(FontIcon.F_WARNING, "orange");
            case 800:
                return icon(FontIcon.F_INFO_CIRCLE, "blue");
            case 700:
                return icon(FontIcon.M_CONTROL_POINT, "green");
            case 500:
                return icon(FontIcon.D_BATTERY_60, "green");
            case 400:
                return icon(FontIcon.D_BATTERY_80, "green");
            case 300:
                return icon(FontIcon.D_BATTERY_CHARGING_100, "green");
            default:
                return icon(FontIcon.D_BATTERY_UNKNOWN, "gray");
        }
    }

    static GlyphIcon<?> icon(@MagicConstant(valuesFromClass = FontIcon.class) String icon, String color) {
        final GlyphIcon<?> glyphIcon = FontIcons.glyphIcon(icon);
        glyphIcon.setStyle("-fx-fill: " + color);
        glyphIcon.setGlyphSize(16);
        return glyphIcon;
    }
}
