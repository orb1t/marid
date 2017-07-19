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

import org.marid.runtime.context.MaridRuntime;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultValueConvertersManager {

    protected final ValueConverters[] valueConverters;

    public DefaultValueConvertersManager(ClassLoader classLoader, MaridRuntime runtime) {
        valueConverters = stream(load(ValueConvertersFactory.class, classLoader).spliterator(), false)
                .map(c -> c.converters(runtime))
                .toArray(ValueConverters[]::new);
    }

    public Function<String, ?> getConverter(String name) {
        return Stream.of(valueConverters)
                .map(c -> c.getConverter(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No converters found for name: " + name));
    }

    public Type getType(String name) {
        for (final ValueConverters converters : valueConverters) {
            final Type type = converters.getTypeMap().get(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }
}
