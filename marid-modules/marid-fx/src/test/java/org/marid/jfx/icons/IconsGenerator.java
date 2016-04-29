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

import com.google.common.collect.ImmutableMap;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class IconsGenerator {

    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception {
        final Properties properties = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("icons.properties")) {
            properties.load(inputStream);
        }
        final Path baseDirPath = Paths.get(properties.getProperty("project.dir"));
        final Path srcPath = baseDirPath.resolve("src").resolve("main").resolve("java");
        final String pkg = FontIcons.class.getPackage().getName();
        final Path iconsPath = Stream.of(pkg.split("[.]")).reduce(srcPath, Path::resolve, (p1, p2) -> p2);
        final Map<String, Class<? extends Enum>> map = ImmutableMap.<String, Class<? extends Enum>>builder()
                .put("O", OctIcon.class)
                .put("M", MaterialIcon.class)
                .put("D", MaterialDesignIcon.class)
                .put("F", FontAwesomeIcon.class)
                .put("W", WeatherIcon.class)
                .build();
        try (final PrintStream p = new PrintStream(iconsPath.resolve("FontIcon.java").toFile(), "UTF-8")) {
            p.format("package %s;%n%n", pkg);
            p.format("public interface FontIcon {%n%n");
            for (final Map.Entry<String, Class<? extends Enum>> e : map.entrySet()) {
                final String prefix = e.getKey();
                final Class<? extends Enum> enumClass = e.getValue();
                final EnumSet<?> set = EnumSet.allOf(enumClass);
                for (final Object oIcon : set) {
                    final GlyphIcons icon = (GlyphIcons) oIcon;
                    final String key = prefix + "_" + icon.name();
                    p.format("    String %s = \"%s\";%n", key, key);
                }
            }
            p.format("}%n");
        }
    }
}
