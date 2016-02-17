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

package org.marid.ide.icons;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphIcons;
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
import org.marid.util.MaridClassValue;
import org.marid.util.Utils;

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeIcons {

    private static final ClassValue<Function<GlyphIcons, GlyphIcon<?>>> ICV = new MaridClassValue<>(c -> {
        if (c == FontAwesomeIcon.class) {
            return i -> new FontAwesomeIconView((FontAwesomeIcon) i);
        } else if (c == MaterialDesignIcon.class) {
            return i -> new MaterialDesignIconView((MaterialDesignIcon) i);
        } else if (c == MaterialIcon.class) {
            return i -> new MaterialIconView((MaterialIcon) i);
        } else if (c == WeatherIcon.class) {
            return i -> new WeatherIconView((WeatherIcon) i);
        } else if (c == OctIcon.class) {
            return i -> new OctIconView((OctIcon) i);
        } else {
            throw new IllegalArgumentException("Icon " + c + " is not supported");
        }
    });

    public static <E extends Enum<E> & GlyphIcons, I extends GlyphIcon<E>> I ideIcon(E type, Number size) {
        return Utils.cast(glyphIcon(type, size));
    }

    public static <E extends Enum<E> & GlyphIcons, I extends GlyphIcon<E>> I ideIcon(E type) {
        return ideIcon(type, 0);
    }

    public static GlyphIcon<?> glyphIcon(GlyphIcons type, Number size) {
        final GlyphIcon<?> icon = ICV.get(type.getClass()).apply(type);
        if (size.intValue() > 0) {
            icon.setGlyphSize(size);
        }
        return Utils.cast(icon);
    }

    public static GlyphIcon<?> glyphIcon(GlyphIcons type) {
        return glyphIcon(type, 0);
    }
}
