/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.expression;

import javax.annotation.Nonnull;

import static org.marid.runtime.expression.NullExpr.NULL;

public class StaticFieldGetExpr extends AbstractExpression implements StaticFieldGetExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private String field;

    public StaticFieldGetExpr(@Nonnull Expression target, @Nonnull String field) {
        this.target = target;
        this.field = field;
    }

    public StaticFieldGetExpr() {
        target = NULL;
        field = "";
    }

    @Override
    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nonnull Expression target) {
        this.target = target;
    }

    @Override
    @Nonnull
    public String getField() {
        return field;
    }

    @Override
    public void setField(@Nonnull String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return target + "!" + field;
    }
}
