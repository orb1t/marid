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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.marid.io.Xmls.attribute;
import static org.marid.runtime.expression.MethodCallExpr.args;
import static org.marid.runtime.expression.MethodCallExpr.target;
import static org.marid.runtime.expression.NullExpr.NULL;

public class MethodCallStaticExpr extends AbstractExpression implements MethodCallStaticExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private String method;

    @Nonnull
    private final List<Expression> args;

    public MethodCallStaticExpr(@Nonnull Expression target, @Nonnull String method, @Nonnull Expression... args) {
        this.target = target;
        this.method = method;
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public MethodCallStaticExpr() {
        target = NULL;
        method = "";
        args = new ArrayList<>();
    }

    @Override
    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Override
    @Nonnull
    public String getMethod() {
        return method;
    }

    @Override
    @Nonnull
    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("method", method);
        target(element, target);
        args(element, args);
    }

    @Override
    public void loadFrom(@Nonnull Element element) {
        target = target(element, NULL::from);
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        args.addAll(args(element, NULL::from));
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "!" + method + "(", ")"));
    }
}
