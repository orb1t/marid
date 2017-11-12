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

import org.marid.expression.generic.ArrayExpression;
import org.marid.io.Xmls;
import org.marid.runtime.context.BeanContext;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class ArrayExpr extends Expr implements ArrayExpression {

  @Nonnull
  private final String elementType;

  @Nonnull
  private final List<Expr> elements;

  public ArrayExpr(@Nonnull String elementType, @Nonnull Expr... elements) {
    this.elementType = elementType;
    this.elements = Arrays.asList(elements);
  }

  ArrayExpr(@Nonnull Element element) {
    super(element);
    elementType = Xmls.attribute(element, "type").orElseThrow(() -> new NullPointerException("type"));
    elements = Xmls.elements("elements", element).map(Expr::of).collect(toList());
  }

  @Nonnull
  @Override
  public String getElementType() {
    return elementType;
  }

  @Nonnull
  @Override
  public List<Expr> getElements() {
    return elements;
  }

  @Override
  protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
    final Class<?> elementClass;
    try {
      elementClass = MaridRuntimeUtils.loadClass(getElementType(), context.getClassLoader(), true);
    } catch (ClassNotFoundException x) {
      throw new IllegalStateException(x);
    }
    final Object array = Array.newInstance(elementClass, elements.size());
    for (int i = 0; i < elements.size(); i++) {
      Array.set(array, i, elements.get(i).evaluate(self, context));
    }
    return array;
  }
}
