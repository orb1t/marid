/*-
 * #%L
 * marid-ide
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

package org.marid.idefx.expression;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.marid.XmlWritable;
import org.marid.expression.generic.Expression;
import org.marid.expression.xml.XmlExpression;
import org.marid.jfx.props.ObservablesProvider;
import org.marid.xml.Tagged;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.deepEquals;

public abstract class Expr implements Expression, ObservablesProvider, Tagged, XmlWritable {

  public final ObservableList<Expr> initializers;

  public Expr() {
    initializers = ObservablesProvider.list();
  }

  Expr(@Nonnull Element element) {
    initializers = XmlExpression.initializers(element, Expr::of, ObservablesProvider.toObservableList());
  }

  @Nonnull
  @Override
  public ObservableList<Expr> getInitializers() {
    return initializers;
  }

  @Nonnull
  @Override
  public String getTag() {
    return getClass().getSimpleName().replace("Expr", "").toLowerCase();
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    XmlExpression.initializers(element, getInitializers());
  }

  private Stream<Object> stream() {
    return ostream().map(v -> v instanceof ObservableValue ? ((ObservableValue) v).getValue() : v);
  }

  @Override
  public int hashCode() {
    return stream().mapToInt(Objects::hashCode).reduce(0, (a, e) -> 31 * a + e);
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null
        && obj.getClass() == getClass()
        && deepEquals(stream().toArray(), ((Expr) obj).stream().toArray());
  }

  public static Expr of(@Nonnull Element element) {
    switch (element.getTagName()) {
      case "array": return new ArrayExpr(element);
      case "class": return new ClassExpr(element);
      case "this": return new ThisExpr(element);
      case "string": return new StringExpr(element);
      case "ref": return new RefExpr(element);
      case "get": return new GetExpr(element);
      case "set": return new SetExpr(element);
      case "null": return new NullExpr(element);
      case "call": return new CallExpr(element);
      default: throw new IllegalArgumentException(element.getTagName());
    }
  }
}
