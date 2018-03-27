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

package org.marid.app.util;

import java.util.Arrays;
import java.util.ListIterator;

public final class HandlerPath {

  private final String[] path;

  public HandlerPath(String path) {
    final String trimmed = path.startsWith("/") ? path.substring(1) : path;
    this.path = trimmed.isEmpty() ? new String[0] : trimmed.split("/");
  }

  private HandlerPath(String... path) {
    this.path = path;
  }

  public int getComponentCount() {
    return path.length;
  }

  public HandlerPath subPath(int len) {
    if (len >= 0 && len < path.length) {
      return new HandlerPath(Arrays.copyOf(path, len));
    } else if (len == path.length) {
      return this;
    } else {
      throw new IndexOutOfBoundsException("Invalid length: " + len);
    }
  }

  public HandlerPath parent() {
    return new HandlerPath(Arrays.copyOf(path, path.length - 1));
  }

  public String component(int index) {
    return path[index];
  }

  public String last() {
    return path[path.length - 1];
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o != null && o.getClass() == HandlerPath.class && Arrays.equals(((HandlerPath) o).path, path);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(path);
  }

  public ListIterator<String> iterator() {
    return Arrays.asList(path).listIterator();
  }

  @Override
  public String toString() {
    return String.join("/", path);
  }
}
