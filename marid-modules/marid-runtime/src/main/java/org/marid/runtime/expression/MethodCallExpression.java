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

import org.marid.io.Xmls;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class MethodCallExpression extends Expression {

    private final String target;
    private final String method;
    private final List<Expression> args;

    public MethodCallExpression(String target, String method, Expression... args) {
        this.target = target;
        this.method = method;
        this.args = Arrays.asList(args);
    }

    public MethodCallExpression(Element element) {
        target = Xmls.attribute(element, "target").orElseThrow(() -> new NullPointerException("target"));
        method = Xmls.attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        args = Xmls.nodes(element, Element.class)
                .filter(e -> "args".equals(e.getTagName()))
                .flatMap(e -> Xmls.nodes(e, Element.class))
                .map(Expression::from)
                .collect(Collectors.toList());
    }

    public String getTarget() {
        return target;
    }

    public String getMethod() {
        return method;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public String getTag() {
        return "call";
    }

    @Override
    public void saveTo(Element element) {
        final Document document = element.getOwnerDocument();
        final Element args = document.createElement("args");
        element.appendChild(args);
        for (final Expression arg : this.args) {
            final Element e = document.createElement(arg.getTag());
            arg.saveTo(e);
            args.appendChild(e);
        }
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "." + method + "(", ")"));
    }
}
