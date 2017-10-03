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
import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;
import static org.marid.io.Xmls.*;

public class ApplyExpr extends AbstractExpression implements ApplyExpression {

    @Nonnull
    private Expression target;

    @Nonnull
    private String method;

    @Nonnull
    private String type;

    @Nonnull
    private final Map<String, Expression> args;

    public ApplyExpr(@Nonnull Expression target,
                     @Nonnull String method,
                     @Nonnull String type,
                     @Nonnull Map<String, Expression> args) {
        this.target = target;
        this.method = method;
        this.type = type;
        this.args = args;
    }

    public ApplyExpr() {
        target = NullExpr.NULL;
        method = "";
        type = "";
        args = new LinkedHashMap<>();
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
    public String getType() {
        return type;
    }

    @Override
    @Nonnull
    public Map<String, Expression> getArgs() {
        return args;
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("method", method);
        element.setAttribute("type", type);
        MethodCallExpr.target(element, target);

        final Document document = element.getOwnerDocument();

        this.args.forEach((name, arg) -> {
            final Element e = document.createElement("arg");
            element.appendChild(e);
            e.setAttribute("name", name);
            final Element v = arg.newElement(element);
            e.appendChild(v);
            arg.saveTo(v);
        });
    }

    @Override
    public void loadFrom(@Nonnull Element element) {
        target = elements(element)
                .filter(e -> "target".equals(e.getTagName()))
                .flatMap(Xmls::elements)
                .map(this::from)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("target"));
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        type = attribute(element, "type").orElseThrow(() -> new NullPointerException("type"));
        nodes(element, Element.class)
                .filter(e -> "arg".equals(e.getTagName()))
                .map(e -> {
                    final String name = attribute(e, "name").orElseThrow(() -> new NullPointerException("name"));
                    final Expression a = nodes(e, Element.class)
                            .map(this::from)
                            .findFirst()
                            .orElseThrow(NoSuchElementException::new);
                    return Map.entry(name, a);
                })
                .forEach(e -> args.put(e.getKey(), e.getValue()));
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final Object t = requireNonNull(target.evaluate(self, context), "target");
        final Class<?> c = target instanceof ClassExpr ? (Class<?>) t : target.getClass();
        final String typeName = context.resolvePlaceholders(this.type);
        final Class<?> type;
        try {
            type = context.getClassLoader().loadClass(typeName);
        } catch (ClassNotFoundException x) {
            throw new IllegalArgumentException(typeName);
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Wrong interface " + type);
        }
        final Method fMethod = of(type.getMethods())
                .filter(m -> !m.isDefault())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No functional interface: " + typeName));
        final String mName = context.resolvePlaceholders(method);
        final Map<String, Object> ps = args.entrySet().stream()
                .collect(toMap(e -> context.resolvePlaceholders(e.getKey()), e -> e.getValue().evaluate(self, context)));

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        if ("new".equals(mName) && target instanceof ClassExpr) {
            for (final Constructor<?> ct : c.getConstructors()) {
                if (ct.getParameterCount() != args.size() + fMethod.getParameterCount()) {
                    continue;
                }
                final Parameter[] parameters = ct.getParameters();
                final Map<String, Integer> indices = new HashMap<>(ps.size());
                for (int i = 0; i < parameters.length; i++) {
                    if (ps.containsKey(parameters[i].getName())) {
                        indices.put(parameters[i].getName(), i);
                    }
                }
                if (indices.size() != ps.size()) {
                    continue;
                }
                try {
                    ct.setAccessible(true);
                    MethodHandle h = lookup.unreflectConstructor(ct).asFixedArity();
                    for (final Entry<String, Integer> e : indices.entrySet()) {
                        h = MethodHandles.insertArguments(h, e.getValue(), ps.get(e.getKey()));
                    }
                    return MethodHandleProxies.asInterfaceInstance(type, h);
                } catch (IllegalAccessException x) {
                    throw new IllegalStateException(x);
                }
            }
            throw new NoSuchElementException();
        } else {
            for (final Method m : c.getMethods()) {
                if (m.getParameterCount() != args.size() + fMethod.getParameterCount()) {
                    continue;
                }
                if (!m.getName().equals(mName)) {
                    continue;
                }
                final Parameter[] parameters = m.getParameters();
                final Map<String, Integer> indices = new HashMap<>(ps.size());
                for (int i = 0; i < parameters.length; i++) {
                    if (ps.containsKey(parameters[i].getName())) {
                        indices.put(parameters[i].getName(), i);
                    }
                }
                if (indices.size() != ps.size()) {
                    continue;
                }
                try {
                    m.setAccessible(true);
                    MethodHandle h = lookup.unreflect(m).asFixedArity();
                    if (!Modifier.isStatic(m.getModifiers())) {
                        h = h.bindTo(t);
                    }
                    for (final Entry<String, Integer> e : indices.entrySet()) {
                        h = MethodHandles.insertArguments(h, e.getValue(), ps.get(e.getKey()));
                    }
                    return MethodHandleProxies.asInterfaceInstance(type, h);
                } catch (IllegalAccessException x) {
                    throw new IllegalStateException(x);
                }
            }
            if (ps.isEmpty() && fMethod.getParameterCount() == 0) {
                for (final Field f : c.getFields()) {
                    if (!f.getName().equals(mName)) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        MethodHandle h = lookup.unreflectGetter(f);
                        if (!Modifier.isStatic(f.getModifiers())) {
                            h = h.bindTo(t);
                        }
                        return MethodHandleProxies.asInterfaceInstance(type, h);
                    } catch (IllegalAccessException x) {
                        throw new IllegalStateException(x);
                    }
                }
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public String toString() {
        return target + ":" + method + ":" + type + args;
    }
}
