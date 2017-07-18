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

import org.marid.annotation.MetaInfo;
import org.marid.annotation.MetaLiteral;
import org.marid.misc.Casts;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractValueConverters implements ValueConverters {

    public static final Pattern COMMA = Pattern.compile(",");

    protected final HashMap<Type, Map<String, MetaLiteral>> converters = new HashMap<>();
    protected final HashMap<String, Function<String, ?>> map = new HashMap<>();

    public AbstractValueConverters() {
        Stream.of(getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(MetaInfo.class))
                .filter(m -> Function.class == m.getReturnType())
                .filter(m -> m.getGenericReturnType() instanceof ParameterizedType)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments().length == 2)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0] == String.class)
                .forEach(method -> {
                    final MetaInfo info = method.getAnnotation(MetaInfo.class);
                    final ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
                    final Type type = pt.getActualTypeArguments()[1];
                    converters.computeIfAbsent(type, k -> new TreeMap<>()).put(info.name(), new MetaLiteral(info));
                    try {
                        final Function<String, ?> function = Casts.cast(method.invoke(this));
                        map.put(info.name(), function);
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException(x);
                    }
                });
    }

    protected <T> void register(MetaLiteral info, Class<T> type, Function<String, T> function) {
        converters.computeIfAbsent(type, k -> new TreeMap<>()).put(info.name, info);
        map.put(info.name, function);
    }

    @Override
    public Function<String, ?> getConverter(String name) {
        return map.get(name);
    }

    @Override
    public HashMap<Type, Map<String, MetaLiteral>> getConverters() {
        return converters;
    }
}
