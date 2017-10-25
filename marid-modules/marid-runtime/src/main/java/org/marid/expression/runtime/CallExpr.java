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

import org.marid.expression.generic.CallExpression;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.marid.io.Xmls.*;

public final class CallExpr extends Expr implements CallExpression {

    @Nonnull
    private Expr target;

    @Nonnull
    private String method;

    @Nonnull
    private final List<Expr> args;

    public CallExpr(@Nonnull Expr target, @Nonnull String method, @Nonnull Expr... args) {
        this.target = target;
        this.method = method;
        this.args = Arrays.asList(args);
    }

    public CallExpr(@Nonnull Element element) {
        super(element);
        target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        args = elements("args", element).map(Expr::of).collect(toList());
    }

    @Override
    @Nonnull
    public Expr getTarget() {
        return target;
    }

    @Override
    @Nonnull
    public String getMethod() {
        return method;
    }

    @Override
    @Nonnull
    public List<Expr> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "." + method + "(", ")"));
    }
}
