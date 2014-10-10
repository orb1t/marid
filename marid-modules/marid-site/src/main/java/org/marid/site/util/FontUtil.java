/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.site.util;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class FontUtil {

    public static Font overrideFont(Font font, int height, int style) {
        return overrideFont(font, d -> new FontData(
                d.getName(),
                height < 0 ? d.getHeight() : height,
                style < 0 ? d.getStyle() : style));
    }

    public static Font overrideFont(Font font, Function<FontData, FontData> mapping) {
        return new Font(font.getDevice(), Arrays.stream(font.getFontData()).map(mapping).toArray(FontData[]::new));
    }
}
