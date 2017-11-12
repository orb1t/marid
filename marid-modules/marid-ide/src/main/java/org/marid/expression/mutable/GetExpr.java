/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.expression.mutable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.types.expression.TypedGetExpression;
import org.marid.jfx.props.FxObject;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.io.Xmls.*;

public class GetExpr extends Expr implements TypedGetExpression {

  public final FxObject<Expr> target;
  public final StringProperty field;

  public GetExpr(@Nonnull Expr target, @Nonnull String field) {
    this.target = new FxObject<>(Expr::getObservables, target);
    this.field = new SimpleStringProperty(field);
  }

  GetExpr(@Nonnull Element element) {
    this.target = new FxObject<>(
        Expr::getObservables,
        element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"))
    );
    this.field = new SimpleStringProperty(
        attribute(element, "field").orElseThrow(() -> new NullPointerException("field"))
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

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    create(element, "target", t -> create(t, getTarget().getTag(), getTarget()::writeTo));
    element.setAttribute("field", getField());
  }
}
