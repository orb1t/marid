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
import org.marid.runtime.types.TypeContext;
import org.marid.runtime.util.ReflectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.runtime.context.MaridRuntimeUtils.value;
import static org.marid.runtime.util.TypeUtils.map;

public interface ConstructorCallExpression extends Expression {

    @Nonnull
    Expression getTarget();

    @Nonnull
    List<? extends Expression> getArgs();

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        return ReflectUtils.evaluate(this::execute, this).apply(self, context);
    }

    @Nonnull
    @Override
    default Type getType(@Nullable Type owner, @Nonnull TypeContext typeContext) {
        final Type t = getTarget().getType(owner, typeContext);
        final Class<?> targetClass = typeContext.getRaw(t);
        return Stream.of(targetClass.getConstructors())
                .filter(m -> m.getParameterCount() == getArgs().size())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> {
                    final Type[] pt = m.getGenericParameterTypes();
                    for (int i = 0; i < pt.length; i++) {
                        final Type at = getArgs().get(i).getType(owner, typeContext);
                        if (!typeContext.isAssignable(pt[i], at)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .map(m -> typeContext.resolve(
                        null,
                        typeContext.getType(m.getDeclaringClass()),
                        map(m.getGenericParameterTypes(), i -> getArgs().get(i).getType(owner, typeContext)))
                )
                .orElseGet(typeContext::getWildcard);
    }

    private Object execute(@Nullable Object self, @Nonnull BeanContext context) {
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
    }
}
