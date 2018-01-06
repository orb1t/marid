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

package org.marid.expression.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.expression.generic.ApplyExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.runtime.context.BeanContext;
import org.marid.types.Classes;
import org.marid.types.Types;
import org.marid.types.invokable.Invokables;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

public final class ApplyExpr extends CallExpr implements ApplyExpression {

  @NotNull
  private final String type;

  @NotNull
  private final int[] indices;

  public ApplyExpr(@NotNull Expr target,
                   @NotNull String method,
                   @NotNull String type,
                   @NotNull int[] indices,
                   @NotNull Expr... args) {
    super(target, method, args);
    this.type = type;
    this.indices = indices;
  }

  ApplyExpr(@NotNull Element element) {
    super(element);
    this.type = XmlExpression.type(element);
    this.indices = XmlExpression.indices(element);
  }

  @NotNull
  @Override
  public String getType() {
    return type;
  }

  @NotNull
  @Override
  public int[] getIndices() {
    return indices;
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type selfType, @NotNull BeanContext context) {
    return context.getClass(type)
        .map(type -> Classes.getSam(type)
            .map(m -> {
              final Type t = getTarget().getType(selfType, context);
              final Type[] types = getArgs().stream().map(a -> a.getType(selfType, context)).toArray(Type[]::new);
              return Types.rawClasses(t)
                  .flatMap(c -> Invokables.invokables(c, getMethod()).filter(i -> i.matches(types)))
                  .findFirst()
                  .map(i -> {
                    final Object obj = getTarget().evaluate(self, selfType, context);
                    final Object[] args = getArgs().stream().map(e -> e.evaluate(self, selfType, context)).toArray();
                    return i.apply(context.getClassLoader(), type, getIndices(), obj, args);
                  });
            })
            .orElseThrow(IllegalStateException::new)
        ).orElseThrow(IllegalStateException::new);
  }
}
