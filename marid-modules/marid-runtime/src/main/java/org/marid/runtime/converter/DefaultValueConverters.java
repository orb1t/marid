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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.marid.annotation.MetaLiteral.l;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultValueConverters extends AbstractValueConverters {

    private final ClassLoader classLoader;

    public DefaultValueConverters(MaridRuntime runtime) {
        classLoader = runtime.getClassLoader();

        register(l("Basic", "String", "D_TOOLTIP_TEXT", "String"), String.class, (v, c) -> v);
        register(l("Basic", "Character", "D_NUMERIC", "Character"), Character.class, (v, c) -> v == null ? null : (char) (int) Integer.decode(v));

        redirect(l("Basic", "Integer", "D_NUMERIC", "Integer"), Integer.class, valueOf()::apply);
        redirect(l("Basic", "Long", "D_NUMERIC", "Long"), Long.class, valueOf()::apply);
        redirect(l("Basic", "Short", "D_NUMERIC", "Short"), Short.class, valueOf()::apply);
        redirect(l("Basic", "Byte", "D_NUMERIC", "Byte"), Byte.class, valueOf()::apply);
        redirect(l("Basic", "Boolean", "D_NUMERIC", "Boolean"), Boolean.class, valueOf()::apply);
        redirect(l("Basic", "Float", "D_NUMERIC", "Float"), Float.class, valueOf()::apply);
        redirect(l("Basic", "Double", "D_NUMERIC", "Double"), Double.class, valueOf()::apply);
        redirect(l("Basic", "BigInteger", "D_NUMERIC", "BigInteger"), BigInteger.class, valueOf()::apply);
        redirect(l("Basic", "BigDecimal", "D_NUMERIC", "BigDecimal"), BigDecimal.class, valueOf()::apply);

        register(l("Special", "ref", "D_SERVER", "Bean by name"), Object.class, (v, c) -> runtime.getBean(v));
        register(l("Special", "runtime", "D_RUN", "Runtime"), MaridRuntime.class, (v, c) -> runtime);
    }

    @MetaInfo(group = "Special", name = "of", icon = "D_FORMAT_TEXT", description = "ValueOf conversion")
    public BiFunction<String, Class<?>, ?> valueOf() {
        return (v, c) -> {
            switch (c.getName()) {
                case "java.lang.String":
                case "java.lang.CharSequence":
                    return v;
                case "java.math.BigInteger":
                    return v == null ? null : new BigInteger(v);
                case "java.math.BigDecimal":
                    return v == null ? null : new BigDecimal(v);
                case "java.util.Locale":
                    return v == null ? null : Locale.forLanguageTag(v);
                case "java.util.Currency":
                    return v == null ? null : Currency.getInstance(v);
                case "java.util.TimeZone":
                    return v == null ? null : TimeZone.getTimeZone(v);
                case "java.time.ZoneId":
                    return v == null ? null : ZoneId.of(v);
                case "java.time.Instant":
                    return v == null ? null : Instant.parse(v);
                case "java.time.Duration":
                    return v == null ? null : Duration.parse(v);
                case "java.nio.Charset":
                    return v == null ? null : Charset.forName(v);
                case "int":
                    return v == null ? 0 : Integer.valueOf(v);
                case "long":
                    return v == null ? 0L : Long.valueOf(v);
                case "float":
                    return v == null ? 0f : Float.valueOf(v);
                case "double":
                    return v == null ? 0d : Double.valueOf(v);
                case "char":
                    return v == null ? (char) 0 : (char) (int) Integer.decode(v);
                case "boolean":
                    return v == null ? Boolean.FALSE : Boolean.valueOf(v);
                case "byte":
                    return v == null ? (byte) 0 : Byte.valueOf(v);
                case "short":
                    return v == null ? (short) 0 : Short.valueOf(v);
                case "java.lang.Class": {
                    try {
                        if (v == null) {
                            return null;
                        } else {
                            return classLoader.loadClass(v);
                        }
                    } catch (ClassNotFoundException x) {
                        throw new IllegalArgumentException(v, x);
                    }
                }
                default: {
                    if (v == null) {
                        return null;
                    } else {
                        final MethodType methodType = MethodType.methodType(c, String.class);
                        try {
                            final MethodHandle h = MethodHandles.publicLookup().findStatic(c, "valueOf", methodType);
                            return h.invokeWithArguments(v);
                        } catch (RuntimeException x) {
                            throw x;
                        } catch (Throwable x) {
                            throw new IllegalStateException(x);
                        }
                    }
                }
            }
        };
    }

    @MetaInfo(group = "Collections", name = "Set<String>", icon = "D_ARRANGE_SEND_TO_BACK", description = "Set of strings")
    public BiFunction<String, Class<?>, Set<String>> convertToSet() {
        return (v, c) -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toSet());
    }

    @MetaInfo(group = "Collections", name = "TreeSet<String>", icon = "D_ARRANGE_SEND_TO_BACK", description = "Sorted set of strings")
    public BiFunction<String, Class<?>, TreeSet<String>> convertToSortedSet() {
        return (v, c) -> COMMA.splitAsStream(v).map(String::trim).collect(Collectors.toCollection(TreeSet::new));
    }

    @MetaInfo(group = "Arrays", name = "int[]", icon = "D_NUMERIC_8_BOX_MULTIPLE_OUTLINE", description = "Int array")
    public BiFunction<String, Class<?>, int[]> convertToIntArray() {
        return (v, c) -> COMMA.splitAsStream(v).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    @MetaInfo(group = "Arrays", name = "long[]", icon = "D_FORMAT_LIST_NUMBERS", description = "Long array")
    public BiFunction<String, Class<?>, long[]> convertToLongArray() {
        return (v, c) -> COMMA.splitAsStream(v).map(String::trim).mapToLong(Long::parseLong).toArray();
    }

    @MetaInfo(group = "Arrays", name = "String[]", icon = "D_COMMENT_TEXT", description = "String array")
    public BiFunction<String, Class<?>, String[]> convertToStringArray() {
        return (v, c) -> COMMA.splitAsStream(v).map(String::trim).toArray(String[]::new);
    }
}
