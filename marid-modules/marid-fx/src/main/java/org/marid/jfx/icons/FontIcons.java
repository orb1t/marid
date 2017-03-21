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

package org.marid.jfx.icons;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.OctIconView;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIconView;
import org.intellij.lang.annotations.MagicConstant;

/**
 * @author Dmitry Ovchinnikov
 */
public class FontIcons {

    public static GlyphIcon<?> glyphIcon(@MagicConstant(valuesFromClass = FontIcon.class) String type, Number size) {
        GlyphIcon<?> icon;
        if (type == null || type.isEmpty()) {
            icon = new FontAwesomeIconView(FontAwesomeIcon.SMILE_ALT);
        } else {
            try {
                switch (type.charAt(0)) {
                    case 'O':
                        icon = new OctIconView(OctIcon.valueOf(type.substring(2)));
                        break;
                    case 'W':
                        icon = new WeatherIconView(WeatherIcon.valueOf(type.substring(2)));
                        break;
                    case 'M':
                        icon = new MaterialIconView(MaterialIcon.valueOf(type.substring(2)));
                        break;
                    case 'D':
                        icon = new MaterialDesignIconView(MaterialDesignIcon.valueOf(type.substring(2)));
                        break;
                    case 'F':
                        icon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(type.substring(2)));
                        break;
                    default:
                        icon = new WeatherIconView(WeatherIcon.MOON_0);
                        break;
                }
            } catch (IllegalArgumentException x) {
                icon = new WeatherIconView(WeatherIcon.MOONSET);
            }
        }
        if (size.intValue() > 0) {
            icon.setGlyphSize(size);
        }
        return icon;
    }

    public static GlyphIcon<?> glyphIcon(@MagicConstant(valuesFromClass = FontIcon.class) String type) {
        return glyphIcon(type, 0);
    }
}
