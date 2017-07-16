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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultValueConverters extends AbstractValueConverters {

    private static final Pattern COMMA = Pattern.compile(",");

    public DefaultValueConverters() {
        super(32);

        converters.put(int.class, Integer::valueOf);
        converters.put(long.class, Long::valueOf);
        converters.put(short.class, Short::valueOf);
        converters.put(byte.class, Byte::valueOf);
        converters.put(char.class, s -> (char) (int) Integer.decode(s));
        converters.put(float.class, Float::valueOf);
        converters.put(double.class, Double::valueOf);
        converters.put(boolean.class, Boolean::valueOf);

        converters.put(Integer.class, Integer::valueOf);
        converters.put(Long.class, Long::valueOf);
        converters.put(Short.class, Short::valueOf);
        converters.put(Byte.class, Byte::valueOf);
        converters.put(Character.class, s -> (char) (int) Integer.decode(s));
        converters.put(Float.class, Float::valueOf);
        converters.put(Double.class, Double::valueOf);
        converters.put(Boolean.class, Boolean::valueOf);

        converters.put(BigInteger.class, BigInteger::new);
        converters.put(BigDecimal.class, BigDecimal::new);

        converters.put(String.class, Function.identity());
        converters.put(CharSequence.class, Function.identity());
        converters.put(String[].class, v -> COMMA.splitAsStream(v).map(String::trim).toArray(String[]::new));
    }

    public Function<String, Set<String>> convertToSet() {
        return v -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toSet());
    }

    public Function<String, SortedSet<String>> convertToSortedSet() {
        return v -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toCollection(TreeSet::new));
    }

    public Function<String, NavigableSet<String>> convertToNavigableSet() {
        return v -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toCollection(TreeSet::new));
    }

    public Function<String, int[]> convertToIntArray() {
        return v -> COMMA.splitAsStream(v).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    public Function<String, long[]> convertToLongArray() {
        return v -> COMMA.splitAsStream(v).map(String::trim).mapToLong(Long::parseLong).toArray();
    }
}
