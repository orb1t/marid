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

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public class FontIcons {

    private static final Map<String, String> SYMBOLS = new HashMap<>();

    static {
        final URL url = requireNonNull(ClassLoader.getSystemResource("fonts/meta.properties"));
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
            while (true) {
                final String line = r.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                final int index = line.indexOf('=');
                final String name = line.substring(0, index);
                final String value = String.valueOf((char) Integer.parseInt(line.substring(index + 3), 16));
                SYMBOLS.put(name, value);
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public static Text glyphIcon(@Nonnull @PropertyKey(resourceBundle = "fonts.meta") String type,
                                 double size) {
        if (type.length() < 3) {
            return glyphIcon("F_SMILE_ALT", size);
        } else {
            final Text label = new Text();
            label.setFont(new Font(family(type), size));
            label.setText(SYMBOLS.getOrDefault(type, ""));
            return label;
        }
    }

    public static Text glyph(@Nonnull @PropertyKey(resourceBundle = "fonts.meta") String type,
                             double size,
                             @Nonnull Color color) {
        final Text text = glyphIcon(type, size);
        text.setStroke(color);
        return text;
    }

    private static String family(String type) {
        switch (type.charAt(0)) {
            case 'O':
                return "Octicons";
            case 'D':
                return "MaterialDesignIcons";
            case 'M':
                return "Material Icons";
            case 'F':
                return "FontAwesome";
            case 'W':
                return "Weather Icons";
            default:
                throw new IllegalArgumentException("Unsupported font symbol: " + type);
        }
    }

    public static Node glyphIcon(@PropertyKey(resourceBundle = "fonts.meta") String type) {
        return glyphIcon(type, 0);
    }
}
