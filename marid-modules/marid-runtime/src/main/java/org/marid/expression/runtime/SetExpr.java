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
import org.marid.runtime.context.BeanContext;
import org.marid.types.Classes;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.element;

public final class SetExpr extends Expr implements SetExpression {

  @Nonnull
  private final Expr target;

  @Nonnull
  private final String field;

  @Nonnull
  private final Expr value;

  public SetExpr(@Nonnull Expr target, @Nonnull String field, @Nonnull Expr value) {
    this.target = target;
    this.field = field;
    this.value = value;
  }

  SetExpr(@Nonnull Element element) {
    super(element);
    target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
    field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
    value = element("value", element).map(Expr::of).orElseThrow(() -> new NullPointerException("value"));
  }

  @Override
  protected Object execute(@Nullable Object self, @Nullable Type owner, @Nonnull BeanContext context) {
    final Object target = getTarget().evaluate(self, owner, context);
    final Class<?> targetClass = getTarget().getTargetClass(owner, context);
    final Field field = Stream.of(targetClass.getFields())
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
  @Nonnull
  public Expr getTarget() {
    return target;
  }

  @Override
  @Nonnull
  public String getField() {
    return field;
  }

  @Override
  @Nonnull
  public Expr getValue() {
    return value;
  }
}
