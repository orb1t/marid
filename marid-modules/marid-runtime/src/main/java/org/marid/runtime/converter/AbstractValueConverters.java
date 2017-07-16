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

package org.marid.runtime.converter;

import org.marid.misc.Casts;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractValueConverters implements ValueConverters {

    protected final HashMap<Type, Function<String, ?>> converters;

    public AbstractValueConverters(int manualEntriesCount) {
        final Method[] methods = Stream.of(getClass().getMethods())
                .filter(m -> m.getName().startsWith("convert"))
                .filter(m -> Function.class == m.getReturnType())
                .filter(m -> m.getGenericReturnType() instanceof ParameterizedType)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments().length == 2)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0] == String.class)
                .toArray(Method[]::new);
        converters = new HashMap<>(methods.length + manualEntriesCount);

        for (final Method method : methods) {
            final ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
            final Type type = pt.getActualTypeArguments()[1];
            try {
                final Function<String, ?> function = Casts.cast(method.invoke(this));
                converters.put(type, function);
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public Function<String, ?> getConverter(Type type) {
        return converters.get(type);
    }
}
