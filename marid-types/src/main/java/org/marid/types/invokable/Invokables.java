/*-
 * #%L
 * marid-types
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

package org.marid.types.invokable;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface Invokables {

  @NotNull
  static Stream<Invokable> invokables(@NotNull Class<?> type, @NotNull String method) {
    if ("new".equals(method)) {
      return Stream.of(type.getConstructors()).map(InvokableConstructor::new);
    } else {
      return Stream.concat(
          Stream.of(type.getMethods()).filter(m -> m.getName().equals(method)).map(InvokableMethod::new),
          Stream.of(type.getFields()).filter(f -> f.getName().equals(method)).flatMap(InvokableField::invokables)
      );
    }
  }
}
