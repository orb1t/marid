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
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.io.Xmls.*;

public class SetExpr extends Expr implements SetExpression {

  public final FxObject<Expr> target;
  public final StringProperty field;
  public final FxObject<Expr> value;

  public SetExpr(@Nonnull Expr target, @Nonnull String field, @Nonnull Expr value) {
    this.target = new FxObject<>(Expr::getObservables, target);
    this.field = new SimpleStringProperty(field);
    this.value = new FxObject<>(Expr::getObservables, value);
  }

  SetExpr(@Nonnull Element element) {
    super(element);
    this.target = new FxObject<>(
        Expr::getObservables,
        element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"))
    );
    this.field = new SimpleStringProperty(
        attribute(element, "field").orElseThrow(() -> new NullPointerException("field"))
    );
    this.value = new FxObject<>(
        Expr::getObservables,
        element("value", element).map(Expr::of).orElseThrow(() -> new NullPointerException("value"))
    );
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
    create(element, "target", t -> create(t, getTarget().getTag(), getTarget()::writeTo));
    element.setAttribute("field", getField());
    create(element, "value", v -> create(v, getValue().getTag(), getValue()::writeTo));
  }
}
