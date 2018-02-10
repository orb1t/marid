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

package org.marid.site.model;

import org.jetbrains.annotations.NotNull;

public class JsLibrary {

  @NotNull
  private final String src;

  @NotNull
  private final String integrity;

  @NotNull
  private final String crossorigin;

  public JsLibrary(@NotNull String src, @NotNull String integrity, @NotNull String crossorigin) {
    this.src = src;
    this.integrity = integrity;
    this.crossorigin = crossorigin;
  }

  public JsLibrary(@NotNull String src, @NotNull String integrity) {
    this(src, integrity, "anonymous");
  }

  @NotNull
  public String getSrc() {
    return src;
  }

  @NotNull
  public String getIntegrity() {
    return integrity;
  }

  @NotNull
  public String getCrossorigin() {
    return crossorigin;
  }
}
