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
import java.net.URL;
import java.util.LinkedList;

public class MaridResourceManager implements ResourceManager {

  private final LinkedList<ResourceManager> parents = new LinkedList<>();
  private final String[] prefixes;

  public MaridResourceManager(String... prefixes) {
    this.prefixes = prefixes;
  }

  @Override
  public Resource getResource(String path) throws IOException {
    final String modPath = path.startsWith("/") ? path.substring(1) : path;

    for (final String prefix : prefixes) {

      final String realPath = prefix + modPath;

      final URL url = getClass().getResource(realPath);
      if (url != null) {
        return new URLResource(url, path);
      }
    }

    for (final ResourceManager parent : parents) {

      final Resource resource = parent.getResource(path);
      if (resource != null) {
        return resource;
      }
    }

    return null;
  }

  public MaridResourceManager addParent(ResourceManager maridResourceManager) {
    parents.add(maridResourceManager);
    return this;
  }

  @Override
  public boolean isResourceChangeListenerSupported() {
    return parents.stream().anyMatch(ResourceManager::isResourceChangeListenerSupported);
  }

  @Override
  public void registerResourceChangeListener(ResourceChangeListener listener) {
    parents.stream()
        .filter(ResourceManager::isResourceChangeListenerSupported)
        .forEach(p -> p.registerResourceChangeListener(listener));
  }

  @Override
  public void removeResourceChangeListener(ResourceChangeListener listener) {
    parents.stream()
        .filter(ResourceManager::isResourceChangeListenerSupported)
        .forEach(p -> p.removeResourceChangeListener(listener));
  }

  @Override
  public void close() {
  }
}
