/*-
 * #%L
 * marid-types
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

package org.marid.types.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;

public final class PassedVars {

  public static final PassedVars EMPTY = new PassedVars();

  private final TypeVariable<?>[] vars;

  private PassedVars() {
    vars = new TypeVariable<?>[0];
  }

  private PassedVars(TypeVariable<?>[] old, TypeVariable<?> variable) {
    vars = Arrays.copyOf(old, old.length + 1);
    vars[old.length] = variable;
  }

  public PassedVars add(@NotNull TypeVariable<?> variable) {
    return contains(variable) ? this : new PassedVars(vars, variable);
  }

  public boolean contains(@NotNull TypeVariable<?> variable) {
    for (final TypeVariable<?> var : vars) {
      if (variable.equals(var)) {
        return true;
      }
    }
    return false;
  }
}
