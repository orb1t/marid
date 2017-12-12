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

import javafx.collections.ObservableList;
import org.marid.expression.generic.ArrayExpression;
import org.marid.expression.xml.XmlExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.props.ObservablesProvider.toObservableList;

public class ArrayExpr extends Expr implements ArrayExpression {

  public final ObservableList<Expr> elements;

  public ArrayExpr(@Nonnull Expr... elements) {
    this.elements = observableArrayList(Expr::observables);
    this.elements.setAll(elements);
  }

  ArrayExpr(@Nonnull Element element) {
    super(element);
    this.elements = XmlExpression.arrayElems(element, Expr::of, toObservableList());
  }

  @Nonnull
  @Override
  public List<Expr> getElements() {
    return elements;
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    XmlExpression.arrayElems(element, getElements());
  }

  @Override
  public String toString() {
    return getElements().stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
  }
}
