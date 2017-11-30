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

package org.marid.expression.mutable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.expression.generic.SetExpression;
import org.marid.expression.generic.XmlExpression;
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.jfx.props.ObservablesProvider.object;

public class SetExpr extends Expr implements SetExpression {

  public final FxObject<Expr> target;
  public final StringProperty field;
  public final FxObject<Expr> value;

  public SetExpr(@Nonnull Expr target, @Nonnull String field, @Nonnull Expr value) {
    this.target = new FxObject<>(Expr::observables, target);
    this.field = new SimpleStringProperty(field);
    this.value = new FxObject<>(Expr::observables, value);
  }

  SetExpr(@Nonnull Element element) {
    super(element);
    this.target = object(XmlExpression.target(element, Expr::of, StringExpr::new, RefExpr::new));
    this.field = new SimpleStringProperty(XmlExpression.field(element));
    this.value = object(XmlExpression.value(element, Expr::of, NullExpr::new));
  }

  @Nonnull
  @Override
  public Expr getTarget() {
    return target.get();
  }

  @Nonnull
  @Override
  public String getField() {
    return field.get();
  }

  @Nonnull
  @Override
  public Expr getValue() {
    return value.get();
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    XmlExpression.target(element, getTarget());
    XmlExpression.field(element, getField());
    XmlExpression.value(element, getValue());
  }

  @Override
  public String toString() {
    return "(" + target + "." + field + "=" + value + ")";
  }
}
