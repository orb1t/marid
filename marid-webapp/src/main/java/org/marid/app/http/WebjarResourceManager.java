/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
package org.marid.app.http;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WebjarResourceManager implements ResourceManager {

  private final Map<String, String> entries;

  public WebjarResourceManager(String... libraries) {
    entries = Arrays.stream(libraries)
        .flatMap(library -> {
          final String pom = pom(library);
          try (final InputStream inputStream = getClass().getResourceAsStream(pom)) {
            if (inputStream != null) {
              final Properties properties = new Properties();
              properties.load(inputStream);

              final String version = properties.getProperty("version");
              if (version != null) {
                return Stream.of(Map.entry(library, pattern(version)));
              }
            }
          } catch (IOException x) {
            throw new UncheckedIOException(x);
          }

          return Stream.empty();
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Resource getResource(String path) {
    if (path.length() > 2) {
      final int index = path.indexOf('/', 2);
      if (index >= 0) {
        final String library = path.substring(1, index);
        final String file = path.substring(index + 1);

        final String entry = entries.get(library);
        if (entry != null) {
          final URL url = getClass().getResource(String.format(entry, library, file));
          if (url != null) {
            return new URLResource(url, path);
          }
        }
      }
    }
    return null;
  }

  protected abstract String pom(String library);

  protected abstract String pattern(String version);

  @Override
  public boolean isResourceChangeListenerSupported() {
    return false;
  }

  @Override
  public void registerResourceChangeListener(ResourceChangeListener resourceChangeListener) {
  }

  @Override
  public void removeResourceChangeListener(ResourceChangeListener resourceChangeListener) {
  }

  @Override
  public void close() {
  }
}
