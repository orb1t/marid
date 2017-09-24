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
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.compatible;
import static org.marid.runtime.context.MaridRuntimeUtils.value;
import static org.marid.runtime.expression.MethodCallExpression.args;
import static org.marid.runtime.expression.MethodCallExpression.target;

public class ConstructorCallExpression extends Expression {

    @Nonnull
    private final Expression target;

    @Nonnull
    private final List<Expression> args;

    public ConstructorCallExpression(@Nonnull Expression target, @Nonnull Expression... args) {
        this.target = target;
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public ConstructorCallExpression(@Nonnull Element element) {
        target = target(element);
        args = args(element);
    }

    @Nonnull
    @Override
    public String getTag() {
        return "new";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        target(element, target);
        args(element, args);
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        final Class<?> t = (Class<?>) requireNonNull(target.evaluate(self, context), "target");
        final Object[] ps = args.stream().map(p -> p.evaluate(self, context)).toArray();
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

    @Override
    public String toString() {
        return args.stream().map(Object::toString).collect(joining(",", target + "(", ")"));
    }
}
