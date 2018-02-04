/*-
 * #%L
 * marid-runtime
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

package org.marid.expression.generic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.cellar.BottleContext;
import org.marid.types.Classes;
import org.marid.types.Types;
import org.marid.types.invokable.Invokables;

import java.lang.reflect.Type;

public interface ApplyExpression extends CallExpression {

  @NotNull
  String getType();

  @NotNull
  int[] getIndices();

  @NotNull
  @Override
  default Type getType(@Nullable Type owner, @NotNull BottleContext context) {
    return context.getClass(getType())
        .map(c -> Classes.getSam(c)
            .map(sam -> {
              final Type target = getTarget().getType(owner, context);
              final Type[] args = getArgs().stream().map(e -> e.getType(owner, context)).toArray(Type[]::new);
              return Types.rawClasses(target)
                  .flatMap(rc -> Invokables.invokables(rc, getMethod()).filter(i -> i.matches(args)))
                  .map(i -> i.type(target, c, sam, getIndices(), args))
                  .findFirst()
                  .orElseGet(() -> {
                    context.throwError(new IllegalStateException());
                    return Object.class;
                  });
            })
            .orElseGet(() -> {
              context.throwError(new IllegalStateException());
              return Object.class;
            }))
        .orElseGet(() -> {
          context.throwError(new IllegalStateException());
          return Object.class;
        });
  }
}
