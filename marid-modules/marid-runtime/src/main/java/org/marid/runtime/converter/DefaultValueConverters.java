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
import org.marid.runtime.context.MaridRuntime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Integer.decode;
import static org.marid.annotation.MetaLiteral.l;
import static org.marid.function.Suppliers.elseFunc;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultValueConverters extends AbstractValueConverters {

    public DefaultValueConverters(MaridRuntime object) {
        register(l("Numerics", "int", "D_NUMERIC", "Integer"), int.class, Integer::valueOf);
        register(l("Numerics", "Integer", "D_NUMERIC", "Integer"), Integer.class, elseFunc(Integer::valueOf));

        register(l("Numerics", "long", "D_NUMERIC", "Long"), long.class, Long::valueOf);
        register(l("Numerics", "Long", "D_NUMERIC", "Long"), Long.class, elseFunc(Long::valueOf));

        register(l("Numerics", "short", "D_NUMERIC", "Short"), short.class, Short::valueOf);
        register(l("Numerics", "Short", "D_NUMERIC", "Short"), Short.class, elseFunc(Short::valueOf));

        register(l("Numerics", "byte", "D_NUMERIC", "Byte"), byte.class, Byte::valueOf);
        register(l("Numerics", "Byte", "D_NUMERIC", "Byte"), Byte.class, elseFunc(Byte::valueOf));

        register(l("Numerics", "char", "D_KEY_CHANGE", "Character"), char.class, s -> (char) (int) decode(s));
        register(l("Numerics", "Character", "D_KEY_CHANGE", "Character"), Character.class, elseFunc(s -> (char) (int) decode(s)));

        register(l("Numerics", "float", "D_NUMERIC", "Byte"), float.class, Float::valueOf);
        register(l("Numerics", "Float", "D_NUMERIC", "Byte"), Float.class, elseFunc(Float::valueOf));

        register(l("Numerics", "double", "D_NUMERIC", "Double"), double.class, Double::valueOf);
        register(l("Numerics", "Double", "D_NUMERIC", "Double"), Double.class, elseFunc(Double::valueOf));

        register(l("Numerics", "boolean", "D_NUMERIC_1_BOX_OUTLINE", "Boolean"), boolean.class, Boolean::valueOf);
        register(l("Numerics", "Boolean", "D_NUMERIC_1_BOX_OUTLINE", "Boolean"), Boolean.class, elseFunc(Boolean::valueOf));

        register(l("Numerics", "BigInteger", "D_NUMERIC_9_PLUS_BOX", "BigInteger"), BigInteger.class, elseFunc(BigInteger::new));
        register(l("Numerics", "BigDecimal", "D_NUMERIC_9_PLUS_BOX", "BigDecimal"), BigDecimal.class, elseFunc(BigDecimal::new));

        register(l("Strings", "String", "D_MESSAGE_TEXT", "String"), String.class, Function.identity());

        register(l("Special", "ref", "D_SERVER", "Bean by name"), Object.class, object::getBean);
        register(l("Special", "runtime", "D_RUN", "Runtime"), MaridRuntime.class, v -> object);
    }

    @MetaInfo(group = "Collections", name = "Set<String>", icon = "D_ARRANGE_SEND_TO_BACK", description = "Set of strings")
    public Function<String, Set<String>> convertToSet() {
        return v -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toSet());
    }

    @MetaInfo(group = "Collections", name = "TreeSet<String>", icon = "D_ARRANGE_SEND_TO_BACK", description = "Sorted set of strings")
    public Function<String, TreeSet<String>> convertToSortedSet() {
        return v -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toCollection(TreeSet::new));
    }

    @MetaInfo(group = "Arrays", name = "int[]", icon = "D_NUMERIC_8_BOX_MULTIPLE_OUTLINE", description = "Int array")
    public Function<String, int[]> convertToIntArray() {
        return v -> COMMA.splitAsStream(v).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    @MetaInfo(group = "Arrays", name = "long[]", icon = "D_FORMAT_LIST_NUMBERS", description = "Long array")
    public Function<String, long[]> convertToLongArray() {
        return v -> COMMA.splitAsStream(v).map(String::trim).mapToLong(Long::parseLong).toArray();
    }

    @MetaInfo(group = "Arrays", name = "String[]", icon = "D_COMMENT_TEXT", description = "String array")
    public Function<String, String[]> convertToStringArray() {
        return v -> COMMA.splitAsStream(v).map(String::trim).toArray(String[]::new);
    }
}
