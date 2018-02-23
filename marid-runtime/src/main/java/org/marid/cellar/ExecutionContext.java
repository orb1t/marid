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

package org.marid.cellar;

import org.jetbrains.annotations.NotNull;
import org.marid.cellar.common.Bottle;
import org.marid.runtime.exception.BottleNotFoundException;

public final class ExecutionContext extends BottleContext {

  @NotNull
  private final RackContext rackContext;

  public ExecutionContext(@NotNull Bottle bottle, @NotNull RackContext rackContext, @NotNull ClassLoader classLoader) {
    super(bottle, classLoader);
    this.rackContext = rackContext;
  }

  @NotNull
  public RackContext getRackContext() {
    return rackContext;
  }

  @NotNull
  public Object getReference(@NotNull String name) {
    return rackContext.parents()
        .filter(c -> c.containsBottle(name))
        .map(c -> c.getBottle(name))
        .findFirst()
        .orElseThrow(() -> new BottleNotFoundException(name));
  }
}
