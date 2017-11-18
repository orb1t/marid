/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.jfx.icons;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.icons525.Icons525;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Properties;

import static java.lang.Thread.currentThread;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Dmitry Ovchinnikov
 */
public class IconsGenerator {

  private static final FontDescription[] FONT_DESCRIPTIONS = {
      new FontDescription('O', OctIcon.class),
      new FontDescription('M', MaterialIcon.class),
      new FontDescription('D', MaterialDesignIcon.class),
      new FontDescription('F', FontAwesomeIcon.class),
      new FontDescription('W', WeatherIcon.class),
      new FontDescription('V', Icons525.class)
  };

  public static void main(String... args) throws Exception {
    final Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("icons.properties"));

    final Path baseDirPath = Paths.get(properties.getProperty("project.dir"));
    final Path resources = baseDirPath.resolve("src").resolve("main").resolve("resources");
    final Path binaryResources = baseDirPath.resolve("src").resolve("main").resolve("binary-resources");
    final Path meta = resources.resolve("fonts").resolve("meta.properties");
    final Path fonts = resources.resolve("META-INF").resolve("fonts.mf");
    final Path families = resources.resolve("fonts").resolve("families.properties");

    try (final BufferedWriter m = newBufferedWriter(meta); final BufferedWriter f = newBufferedWriter(fonts)) {
      for (final FontDescription d : FONT_DESCRIPTIONS) {
        for (final GlyphIcons icon : d.iconSet) {
          m.write(d.key);
          m.write('_');
          m.write(icon.name());
          m.write('=');
          m.write(Integer.toUnsignedString(icon.unicode().charAt(0), 16));
          m.newLine();
        }

        final URL url = d.iconSetClass.getResource(d.iconSetClass.getSimpleName() + ".class");
        final int index = url.getPath().indexOf('!');
        final Path jarFile = Paths.get(new URI(url.getPath().substring(0, index)));
        try (final FileSystem fs = newFileSystem(jarFile, currentThread().getContextClassLoader())) {
          final Path base = fs.getPath("/", d.iconSetClass.getName().split("[.]")).getParent();
          final Path font = Files.list(base)
              .filter(p -> p.getFileName().toString().endsWith(".ttf"))
              .findFirst()
              .orElseThrow(FileNotFoundException::new);
          Files.copy(font, binaryResources.resolve(font.getFileName().toString()), REPLACE_EXISTING);

          f.write(d.key);
          f.write('=');
          f.write('/');
          f.write(font.getFileName().toString());
          f.newLine();
        }
      }
    }

    try (final BufferedWriter f = newBufferedWriter(families)) {
      for (final FontDescription d : FONT_DESCRIPTIONS) {
        final GlyphIcons i = d.iconSet.stream().findFirst().orElseThrow(IllegalStateException::new);

        f.write(d.key);
        f.write('=');
        f.write(i.fontFamily().replace("'", ""));
        f.newLine();
      }
    }
  }

  private static class FontDescription {

    private final char key;
    private final Class<? extends GlyphIcons> iconSetClass;
    private final EnumSet<? extends GlyphIcons> iconSet;

    private <E extends Enum<E> & GlyphIcons> FontDescription(char key, Class<E> iconSetClass) {
      this.key = key;
      this.iconSetClass = iconSetClass;
      this.iconSet = EnumSet.allOf(iconSetClass);
    }
  }
}
