/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class MaridContext {

  @Nullable
  private final MaridContext parent;

  @NotNull
  private final String name;

  @NotNull
  private final Map<@NotNull String, @Nullable Object> barrels;

  public MaridContext(@Nullable MaridContext parent,
                      @NotNull String name,
                      @NotNull Map<@NotNull String, @Nullable Object> barrels) {
    this.parent = parent;
    this.name = name;
    this.barrels = barrels;
  }

  @Nullable
  public MaridContext getParent() {
    return parent;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public Map<String, Object> getBarrels() {
    return barrels;
  }
}
