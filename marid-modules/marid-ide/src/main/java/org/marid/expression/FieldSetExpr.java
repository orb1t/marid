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

import org.marid.runtime.expression.Expression;
import org.marid.runtime.expression.FieldSetExpression;

import javax.annotation.Nonnull;

public class FieldSetExpr extends AbstractExpression implements FieldSetExpression {

    @Nonnull
    @Override
    public Expression getTarget() {
        return null;
    }

    @Override
    public void setTarget(@Nonnull Expression target) {

    }

    @Nonnull
    @Override
    public String getField() {
        return null;
    }

    @Override
    public void setField(@Nonnull String field) {

    }

    @Nonnull
    @Override
    public Expression getValue() {
        return null;
    }

    @Override
    public void setValue(@Nonnull Expression value) {

    }
}
