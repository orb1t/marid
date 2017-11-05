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
import org.marid.misc.Calls;
import org.marid.runtime.context.BeanContext;
import org.marid.runtime.context.MaridRuntimeUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    CallExpr(@Nonnull Element element) {
        super(element);
        target = element("target", element).map(Expr::of).orElseThrow(() -> new NullPointerException("target"));
        method = attribute(element, "method").orElseThrow(() -> new NullPointerException("method"));
        args = elements("args", element).map(Expr::of).collect(toList());
    }

    @Override
    protected Object execute(@Nullable Object self, @Nullable Class<?> selfType, @Nonnull BeanContext context) {
        final Class<?> target = getTarget().targetType(context, selfType);
        final Object[] args = getArgs().stream().map(a -> a.evaluate(self, selfType, context)).toArray();
        final Supplier<NoSuchElementException> errorSupplier = () -> new NoSuchElementException(Stream.of(args)
                .map(v -> v == null ? "*" : v.getClass().getName())
                .collect(joining(",", "No such method " + getMethod() + "(", ")")));
        final MethodHandle handle;
        if ("new".equals(getMethod())) {
            handle = Stream.of(target.getConstructors())
                    .filter(c -> MaridRuntimeUtils.compatible(c, args))
                    .findFirst()
                    .map(c -> Calls.call(() -> MethodHandles.publicLookup().unreflectConstructor(c)))
                    .orElseThrow(errorSupplier).asFixedArity();
        } else {
            handle = Stream.of(target.getMethods())
                    .filter(m -> m.getName().equals(getMethod()))
                    .filter(m -> MaridRuntimeUtils.compatible(m, args))
                    .findFirst()
                    .map(m -> Calls.call(() -> {
                        final MethodHandle h = MethodHandles.publicLookup().unreflect(m);
                        if (Modifier.isStatic(m.getModifiers())) {
                            return h;
                        } else {
                            final Object t = getTarget().evaluate(self, selfType, context);
                            return h.bindTo(t);
                        }
                    }))
                    .orElseThrow(errorSupplier).asFixedArity();
        }
        final MethodHandle h = MethodHandles.explicitCastArguments(handle, handle.type().generic());
        try {
            return h.invokeWithArguments(args);
        } catch (RuntimeException | Error x) {
            throw x;
        } catch (Throwable x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    @Nonnull
    public Class<?> getType(@Nonnull BeanContext context, @Nullable Class<?> selfType) {
        final Class<?> target = getTarget().targetType(context, selfType);
        if ("new".equals(getMethod())) {
            return target;
        } else {
            final Class<?>[] types = getArgs().stream().map(a -> a.getType(context, selfType)).toArray(Class<?>[]::new);
            return Stream.of(target.getMethods())
                    .filter(m -> m.getName().equals(getMethod()))
                    .filter(m -> MaridRuntimeUtils.compatible(m, types))
                    .findFirst()
                    .map(Method::getReturnType)
                    .orElseThrow(() -> new NoSuchElementException(Stream.of(types)
                            .map(Class::getName)
                            .collect(joining(",", "No such method " + getMethod() + "(", ")"))));
        }
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
