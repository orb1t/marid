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

import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static org.marid.io.Xmls.attribute;
import static org.marid.runtime.expression.FieldSetExpr.value;
import static org.marid.runtime.expression.MethodCallExpr.target;
import static org.marid.runtime.expression.NullExpr.NULL;

public class FieldSetStaticExpr extends AbstractExpression implements FieldSetStaticExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private String field;

    @Nonnull
    private Expression value;

    public FieldSetStaticExpr(@Nonnull Expression target, @Nonnull String field, @Nonnull Expression value) {
        this.target = target;
        this.field = field;
        this.value = value;
    }

    public FieldSetStaticExpr() {
        target = NULL;
        field = "";
        value = NULL;
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("field", field);
        target(element, target);
        value(element, value);
    }

    @Override
    public void loadFrom(@Nonnull Element element) {
        field = attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
        target = target(element, NULL::from);
        value = value(element, NULL::from);
    }

    @Nonnull
    @Override
    public Expression getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nonnull Expression target) {
        this.target = target;
    }

    @Nonnull
    @Override
    public String getField() {
        return field;
    }

    @Override
    public void setField(@Nonnull String field) {
        this.field = field;
    }

    @Nonnull
    @Override
    public Expression getValue() {
        return value;
    }

    @Override
    public void setValue(@Nonnull Expression value) {
        this.value = value;
    }
}
