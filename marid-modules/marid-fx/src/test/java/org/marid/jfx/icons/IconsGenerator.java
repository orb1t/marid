package org.marid.jfx.icons;

import com.google.common.collect.ImmutableMap;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public class IconsGenerator {

    private static final Map<String, Set<? extends GlyphIcons>> ICONS_MAP = ImmutableMap.of(
            "O", EnumSet.allOf(OctIcon.class),
            "M", EnumSet.allOf(MaterialIcon.class),
            "D", EnumSet.allOf(MaterialDesignIcon.class),
            "F", EnumSet.allOf(FontAwesomeIcon.class),
            "W", EnumSet.allOf(WeatherIcon.class)
    );

    public static void main(String... args) throws Exception {
        final Properties properties = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("icons.properties")) {
            properties.load(inputStream);
        }
        final Path baseDirPath = Paths.get(properties.getProperty("project.dir"));
        final Path resources = baseDirPath.resolve("src").resolve("main").resolve("resources");
        final Path meta = resources.resolve("fonts").resolve("meta.properties");
        try (final BufferedWriter writer = Files.newBufferedWriter(meta)) {
            for (final Map.Entry<String, Set<? extends GlyphIcons>> e : ICONS_MAP.entrySet()) {
                for (final GlyphIcons icon : e.getValue()) {
                    writer.write(e.getKey());
                    writer.write('_');
                    writer.write(icon.name());
                    writer.write('=');
                    writer.write(icon.unicodeToString());
                    writer.newLine();
                }
            }
        }
    }
}
