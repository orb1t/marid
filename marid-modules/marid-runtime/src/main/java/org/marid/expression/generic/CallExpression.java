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

package org.marid.expression.generic;

import org.marid.runtime.context.BeanContext;
import org.marid.runtime.util.ReflectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.runtime.context.MaridRuntimeUtils.value;

public interface CallExpression extends Expression {

    @Nonnull
    Expression getTarget();

    @Nonnull
    String getMethod();

    @Nonnull
    List<? extends Expression> getArgs();

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        return ReflectUtils.eval(execute(self, context), this, context);
    }

    private Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final String mName = context.resolvePlaceholders(getMethod());
        if (getTarget() instanceof ClassExpression) {
            if ("new".equals(mName)) {
                final Class<?> t = (Class<?>) requireNonNull(getTarget().evaluate(self, context), "target");
                final Object[] ps = getArgs().stream().map(p -> p.evaluate(self, context)).toArray();
                final Constructor ct = of(t.getConstructors())
                        .filter(co -> compatible(co, ps))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No constructor found for " + t));
                final Class<?>[] types = ct.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    ps[i] = value(types[i], ps[i]);
                }
                try {
                    ct.setAccessible(true);
                    return ct.newInstance(ps);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException x) {
                    throw new IllegalStateException(x);
                }
            } else {
                final Class<?> t = (Class<?>) requireNonNull(getTarget().evaluate(self, context), "target");
                final Object[] ps = getArgs().stream().map(p -> p.evaluate(null, context)).toArray();
                final Method mt = of(t.getMethods())
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
                    return mt.invoke(null, ps);
                } catch (IllegalAccessException | InvocationTargetException x) {
                    throw new IllegalStateException(x);
                }
            }
        } else {
            final Object t = requireNonNull(getTarget().evaluate(self, context), "target");
            final Object[] ps = getArgs().stream().map(p -> p.evaluate(t, context)).toArray();
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
    }
}
