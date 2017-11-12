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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.marid.types.expression.TypedConstExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.io.Xmls.attribute;

public class ConstExpr extends ValueExpr implements TypedConstExpression {

  public final ObjectProperty<ConstantType> type;

  public ConstExpr(@Nonnull ConstantType type, @Nonnull String value) {
    super(value);
    this.type = new SimpleObjectProperty<>(type);
  }

  ConstExpr(@Nonnull Element element) {
    super(element);
    this.type = new SimpleObjectProperty<>(
        attribute(element, "type").map(ConstantType::valueOf).orElseThrow(() -> new NullPointerException("type"))
    );
  }

  @Nonnull
  @Override
  public ConstantType getType() {
    return type.get();
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    element.setAttribute("type", getType().name());
  }
}
