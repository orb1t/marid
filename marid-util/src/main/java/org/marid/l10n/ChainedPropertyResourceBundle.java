/*-
 * #%L
 * marid-util
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

package org.marid.l10n;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class ChainedPropertyResourceBundle extends ResourceBundle {

  private final Properties[] properties;

  public ChainedPropertyResourceBundle(URL[] urls, boolean useCaches) throws IOException {
    properties = new Properties[urls.length];
    for (int i = 0; i < urls.length; i++) {
      properties[i] = new Properties();
      final URLConnection connection = urls[i].openConnection();
      connection.setUseCaches(useCaches);
      try (final Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
        properties[i].load(reader);
      }
    }
  }

  @Override
  public boolean containsKey(@NotNull String key) {
    return Stream.of(properties).anyMatch(p -> p.containsKey(key)) || parent != null && parent.containsKey(key);
  }

  @Override
  protected String handleGetObject(@NotNull String key) {
    return Stream.of(properties)
        .map(p -> p.getProperty(key))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }

  @NotNull
  @Override
  protected Set<String> handleKeySet() {
    return Stream.of(properties).flatMap(p -> p.stringPropertyNames().stream()).collect(Collectors.toSet());
  }

  @NotNull
  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(keySet());
  }
}
