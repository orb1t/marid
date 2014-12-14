/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ToStringBuilder<T> {

    protected final T target;
    protected String name;
    protected final Map<String, Object> map = new LinkedHashMap<>();

    public ToStringBuilder(T target) {
        this.target = target;
    }

    public ToStringBuilder<T> withName(String name) {
        this.name = name;
        return this;
    }

    public ToStringBuilder<T> withName(Function<T, String> nameFunction) {
        this.name = nameFunction.apply(target);
        return this;
    }

    public ToStringBuilder<T> withEntry(String name, Function<T, Object> valueFunction) {
        map.put(name, valueFunction.apply(target));
        return this;
    }

    public ToStringBuilder<T> withEntry(Function<T, String> nameFunction, Function<T, Object> valueFunction) {
        map.put(nameFunction.apply(target), valueFunction.apply(target));
        return this;
    }

    public ToStringBuilder<T> $(String name) {
        return withName(name);
    }

    public ToStringBuilder<T> $(Function<T, String> nameFunction) {
        return withName(nameFunction);
    }

    public ToStringBuilder<T> $(String name, Function<T, Object> valueFunction) {
        return withEntry(name, valueFunction);
    }

    public ToStringBuilder<T> $(Function<T, String> nameFunction, Function<T, Object> valueFunction) {
        return withEntry(nameFunction, valueFunction);
    }

    public String build() {
        return (name == null ? target.getClass().getSimpleName() : name) + map;
    }

    public String build(String previous) {
        final String name = this.name == null ? target.getClass().getSimpleName() : this.name;
        final StringBuilder builder = new StringBuilder(previous);
        if (!previous.startsWith(name)) {
            final int bracePos = previous.indexOf('{');
            if (bracePos >= 0) {
                builder.replace(0, bracePos, name);
            }
        }
        if (!map.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
            map.entrySet().forEach(e -> builder.append(',').append(e));
            builder.append('}');
        }
        return builder.toString();
    }
}
