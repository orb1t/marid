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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toMap;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.nodes;

public class ApplyExpression extends Expression {

    private final String target;
    private final String method;
    private final String type;
    private final Map<String, Expression> args;

    public ApplyExpression(String target, String method, String type, Map<String, Expression> args) {
        this.target = target;
        this.method = method;
        this.type = type;
        this.args = args;
    }

    public ApplyExpression(Element element) {
        target = attribute(element, "target").orElseThrow(() -> new NullPointerException("target"));
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        type = attribute(element, "type").orElseThrow(() -> new NullPointerException("type"));
        args = nodes(element, Element.class)
                .filter(e -> "arg".equals(e.getTagName()))
                .map(e -> {
                    final String name = attribute(e, "name").orElseThrow(() -> new NullPointerException("name"));
                    final Expression a = nodes(e, Element.class)
                            .map(Expression::from)
                            .findFirst()
                            .orElseThrow(NoSuchElementException::new);
                    return new AbstractMap.SimpleImmutableEntry<>(name, a);
                })
                .collect(toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2, LinkedHashMap::new));
    }

    public String getTarget() {
        return target;
    }

    public String getMethod() {
        return method;
    }

    public String getType() {
        return type;
    }

    public Map<String, Expression> getArgs() {
        return args;
    }

    @Override
    public String getTag() {
        return "apply";
    }

    @Override
    public void saveTo(Element element) {
        element.setAttribute("target", target);
        element.setAttribute("method", method);
        element.setAttribute("type", type);

        final Document document = element.getOwnerDocument();

        this.args.forEach((name, arg) -> {
            final Element e = document.createElement("arg");
            element.appendChild(e);
            e.setAttribute("name", name);
            final Element v = document.createElement(arg.getTag());
            e.appendChild(v);
            arg.saveTo(v);
        });
    }

    @Override
    public String toString() {
        return target + ":" + method + ":" + type + args;
    }
}
