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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BowerResourceManager implements ResourceManager {

  private final Map<String, String> entries;

  public BowerResourceManager(String... libraries) {
    entries = Arrays.stream(libraries)
        .flatMap(library -> {
          final String pom = "/META-INF/maven/org.webjars.bower/" + library + "/pom.properties";
          try (final InputStream inputStream = getClass().getResourceAsStream(pom)) {
            if (inputStream != null) {
              final Properties properties = new Properties();
              properties.load(inputStream);

              final String version = properties.getProperty("version");
              if (version != null) {
                return Stream.of(Map.entry(library, "/META-INF/resources/webjars/%s/" + version + "/dist/%s"));
              }
            }
          } catch (IOException x) {
            throw new UncheckedIOException(x);
          }

          return Stream.empty();
        })
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

  @Override
  public boolean isResourceChangeListenerSupported() {
    return false;
  }

  @Override
  public void registerResourceChangeListener(ResourceChangeListener listener) {
  }

  @Override
  public void removeResourceChangeListener(ResourceChangeListener listener) {
  }

  @Override
  public void close() {
  }
}
