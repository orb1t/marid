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

import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.marid.io.Xmls.attribute;
import static org.marid.io.Xmls.elements;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.runtime.context.MaridRuntimeUtils.value;

public class MethodCallExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final String method;

    @Nonnull
    private final List<Expression> args;

    public MethodCallExpression(@Nonnull Expression target, @Nonnull String method, @Nonnull Expression... args) {
        this.target = target;
        this.method = method;
        this.args = Arrays.asList(args);
    }

    public MethodCallExpression(@Nonnull Element element) {
        target = target(element);
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        args = args(element);
    }

    @Nonnull
    public Expression getTarget() {
        return target;
    }

    @Nonnull
    public String getMethod() {
        return method;
    }

    @Nonnull
    public List<Expression> getArgs() {
        return args;
    }

    @Nonnull
    @Override
    public String getTag() {
        return "call";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("method", method);
        target(element, target);
        args(element, args);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final Object t = requireNonNull(target.evaluate(self, context), "target");
        final String mName = context.resolvePlaceholders(method);
        final Object[] ps = args.stream().map(p -> p.evaluate(t, context)).toArray();
        final Method mt = of(t.getClass().getMethods())
                .filter(m -> mName.equals(m.getName()))
                .filter(m -> compatible(m, ps))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(mName));
        final Class<?>[] types = mt.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            ps[i] = value(types[i], ps[i]);
        }
        try {
            mt.setAccessible(true);
            if (mt.getReturnType() == void.class) {
                mt.invoke(t, ps);
                return t;
            } else {
                return mt.invoke(t, ps);
            }
        } catch (IllegalAccessException | InvocationTargetException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "." + method + "(", ")"));
    }

    public static Expression target(Element element) {
        return elements(element)
                .filter(e -> "target".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(Expression::from))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("target"));
    }

    public static void target(Element element, Expression target) {
        final Document document = element.getOwnerDocument();
        final Element targetElement = build(document.createElement("target"), element::appendChild);
        target.saveTo(build(document.createElement(target.getTag()), targetElement::appendChild));
    }

    public static List<Expression> args(Element element) {
        return elements(element)
                .filter(e -> "args".equals(e.getTagName()))
                .flatMap(e -> elements(e).map(Expression::from))
                .collect(toList());
    }

    public static void args(Element element, List<Expression> args) {
        final Document document = element.getOwnerDocument();
        final Element argsElement = build(document.createElement("args"), element::appendChild);
        args.forEach(arg -> arg.saveTo(build(document.createElement(arg.getTag()), argsElement::appendChild)));
    }
}
