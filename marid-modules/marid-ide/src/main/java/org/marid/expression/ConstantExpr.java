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

package org.marid.expression;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.marid.runtime.expression.ConstantExpression;

import javax.annotation.Nonnull;

public class ConstantExpr extends ValueExpr implements ConstantExpression {

    public final ObjectProperty<ConstantType> type = new SimpleObjectProperty<>();

    public ConstantExpr(@Nonnull ConstantType type, @Nonnull String value) {
        super(value);
        this.type.set(type);
    }

    public ConstantExpr() {
        this(ConstantType.INT, "");
    }

    @Nonnull
    @Override
    public ConstantType getType() {
        return type.get();
    }

    @Override
    public void setType(@Nonnull ConstantType type) {
        this.type.set(type);
    }
}
