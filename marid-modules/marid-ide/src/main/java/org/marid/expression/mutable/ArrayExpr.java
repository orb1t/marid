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

import javafx.collections.ObservableList;
import org.marid.expression.generic.ArrayExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.io.Xmls.create;
import static org.marid.io.Xmls.elements;

public class ArrayExpr extends Expr implements ArrayExpression {

  public final ObservableList<Expr> elements;

  public ArrayExpr(@Nonnull String elementType, @Nonnull Expr... elements) {
    this.elements = observableArrayList(Expr::getObservables);
    this.elements.setAll(elements);
  }

  ArrayExpr(@Nonnull Element element) {
    super(element);
    this.elements = elements("elements", element)
        .map(Expr::of)
        .collect(toCollection(() -> observableArrayList(Expr::getObservables)));
  }

  @Nonnull
  @Override
  public List<Expr> getElements() {
    return elements;
  }

  @Override
  public void writeTo(@Nonnull Element element) {
    super.writeTo(element);
    if (!elements.isEmpty()) {
      create(element, "elements", es -> getElements().forEach(e -> create(es, e.getTag(), e::writeTo)));
    }
  }
}
