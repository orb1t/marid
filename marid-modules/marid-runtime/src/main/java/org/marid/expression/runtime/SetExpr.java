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

package org.marid.expression.runtime;

import org.marid.expression.generic.SetExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.io.Xmls.*;

public final class SetExpr extends Expr implements SetExpression {

    @Nonnull
    private Expr target;

    @Nonnull
    private String field;

    @Nonnull
    private Expr value;

    public SetExpr(@Nonnull Expr target, @Nonnull String field, @Nonnull Expr value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    public SetExpr(@Nonnull Element element) {
        super(element);
        target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        value = element("value", element).map(Expr::of).orElseThrow(() -> new NullPointerException("value"));
    }

    @Override
    @Nonnull
    public Expr getTarget() {
        return target;
    }

    @Override
    @Nonnull
    public String getField() {
        return field;
    }

    @Override
    public void writeTo(@Nonnull Element element) {
        super.writeTo(element);
        create(element, "target", t -> create(t, target.getTag(), target::writeTo));
        element.setAttribute("field", field);
        create(element, "value", v -> create(v, value.getTag(), value::writeTo));
    }

    @Override
    @Nonnull
    public Expr getValue() {
        return value;
    }
}