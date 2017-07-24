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
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractValueConverters implements ValueConverters {

    public static final Pattern COMMA = Pattern.compile(",");

    protected final HashMap<String, MetaLiteral> metaMap = new HashMap<>();
    protected final HashMap<String, ValueConverter> map = new HashMap<>();
    protected final HashMap<String, Type> typeMap = new HashMap<>();

    public AbstractValueConverters() {
        Stream.of(getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(MetaInfo.class))
                .filter(m -> BiFunction.class == m.getReturnType())
                .filter(m -> m.getGenericReturnType() instanceof ParameterizedType)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments().length == 3)
                .filter(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0] == String.class)
                .forEach(method -> {
                    final MetaInfo info = method.getAnnotation(MetaInfo.class);
                    final ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
                    final Type type = pt.getActualTypeArguments()[2];
                    metaMap.put(info.name(), new MetaLiteral(info));
                    typeMap.put(info.name(), type);
                    try {
                        final BiFunction<String, Class<?>, ?> function = Casts.cast(method.invoke(this));
                        map.put(info.name(), function::apply);
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException(x);
                    }
                });
    }

    protected <T> void register(MetaLiteral info, Class<T> type, BiFunction<String, Class<T>, T> converter) {
        metaMap.put(info.name, info);
        typeMap.put(info.name, type);
        map.put(info.name, (v, c) -> converter.apply(v, type));
    }

    protected void redirect(MetaLiteral info, Class<?> type, BiFunction<String, Class<?>, ?> converter) {
        metaMap.put(info.name, info);
        typeMap.put(info.name, type);
        map.put(info.name, (v, c) -> converter.apply(v, type));
    }

    @Override
    public ValueConverter getConverter(String name) {
        return map.get(name);
    }

    @Override
    public HashMap<String, MetaLiteral> getMetaMap() {
        return metaMap;
    }

    @Override
    public HashMap<String, Type> getTypeMap() {
        return typeMap;
    }
}
