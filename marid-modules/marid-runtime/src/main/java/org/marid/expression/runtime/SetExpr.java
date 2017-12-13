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

import org.marid.expression.generic.SetExpression;
import org.marid.expression.xml.XmlExpression;
import org.marid.runtime.context.BeanContext;
import org.marid.types.Classes;
import org.w3c.dom.Element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class SetExpr extends Expr implements SetExpression {

  @NotNull
  private final Expr target;

  @NotNull
  private final String field;

  @NotNull
  private final Expr value;

  public SetExpr(@NotNull Expr target, @NotNull String field, @NotNull Expr value) {
    this.target = target;
    this.field = field;
    this.value = value;
  }

  SetExpr(@NotNull Element element) {
    super(element);
    target = XmlExpression.target(element, Expr::of, ClassExpr::new, RefExpr::new);
    field = XmlExpression.field(element);
    value = XmlExpression.value(element, Expr::of, NullExpr::new);
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @NotNull BeanContext context) {
    final Object target = getTarget().evaluate(self, owner, context);
    final Field field = getTarget().getTargetClass(owner, context)
        .flatMap(c -> Stream.of(c.getFields()))
        .filter(f -> f.getName().equals(getField()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(getField()));
    try {
      final Object v = getValue().evaluate(self, owner, context);
      field.set(target, Classes.value(field.getType(), v));
      return target;
    } catch (IllegalAccessException x) {
      throw new IllegalStateException(x);
    }
  }

  @Override
  @NotNull
  public Expr getTarget() {
    return target;
  }

  @Override
  @NotNull
  public String getField() {
    return field;
  }

  @Override
  @NotNull
  public Expr getValue() {
    return value;
  }
}
