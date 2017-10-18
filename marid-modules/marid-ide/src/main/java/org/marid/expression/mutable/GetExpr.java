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
import org.marid.expression.generic.GetExpression;
import org.marid.jfx.props.FxObject;

import javax.annotation.Nonnull;

public class GetExpr extends Expr implements GetExpression {

    public final FxObject<Expr> target = new FxObject<>(Expr::getObservables);
    public final StringProperty field = new SimpleStringProperty();

    public GetExpr(@Nonnull Expr target, @Nonnull String field) {
        this.target.set(target);
        this.field.set(field);
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
    public org.marid.expression.runtime.Expr toRuntimeExpr() {
        return new org.marid.expression.runtime.GetExpr(getTarget().toRuntimeExpr(), getField());
    }
}
