/*-
 * #%L
 * marid-util
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

package org.marid.misc;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public interface StringUtils {

    static Predicate<Path> pathEndsWith(String suffix) {
        return p -> p.getFileName().toString().endsWith(suffix);
    }

    @Nonnull
    static <E extends Enum<E>> EnumSet<E> enumSetFromString(@Nonnull Class<E> type,
                                                            @Nonnull String list,
                                                            boolean skipUnknown) {
        return Stream.of(list.split(","))
                .map(String::trim)
                .map(e -> {
                    try {
                        return Enum.valueOf(type, e);
                    } catch (IllegalArgumentException x) {
                        if (skipUnknown) {
                            return null;
                        } else {
                            throw x;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .reduce(EnumSet.noneOf(type), (a, e) -> {
                    a.add(e);
                    return a;
                }, (a1, a2) -> a2);
    }

    @Nonnull
    static String enumSetToString(EnumSet<?> set) {
        return set.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
