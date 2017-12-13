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

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;

import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.Thread.currentThread;
import static java.util.Map.ofEntries;
import static org.marid.misc.Casts.cast;
import static org.marid.misc.Urls.lines;

/**
 * @author Dmitry Ovchinnikov
 */
public class FontIcons {

  private static final Map<String, String> SYMBOLS;
  private static final Map<String, String> FAMILIES;

  static {
    SYMBOLS = ofEntries(cast(lines(currentThread().getContextClassLoader(), "fonts/meta.properties")
        .filter(l -> l.contains("="))
        .map(line -> {
          final int index = line.indexOf('=');
          final String name = line.substring(0, index);
          final String value = Character.toString((char) Integer.parseInt(line.substring(index + 1), 16));
          return Map.entry(name, value);
        })
        .toArray(Entry[]::new)));
    FAMILIES = ofEntries(cast(lines(currentThread().getContextClassLoader(), "fonts/families.properties")
        .filter(l -> l.contains("="))
        .map(line -> {
          final int index = line.indexOf('=');
          final String name = line.substring(0, index);
          final String value = line.substring(index + 1);
          return Map.entry(name, value);
        })
        .toArray(Entry[]::new)));
  }

  public static Text glyphIcon(@NotNull @PropertyKey(resourceBundle = "fonts.meta") String type, double size) {
    if (type.length() < 3 || !FAMILIES.containsKey(type.substring(0, 1))) {
      return glyphIcon("F_SMILE_ALT", size);
    } else {
      final Text label = new Text();
      label.setFont(new Font(FAMILIES.get(type.substring(0, 1)), size));
      label.setText(SYMBOLS.getOrDefault(type, ""));
      return label;
    }
  }

  public static Text glyphIcon(@PropertyKey(resourceBundle = "fonts.meta") String type) {
    return glyphIcon(type, 16);
  }
}
