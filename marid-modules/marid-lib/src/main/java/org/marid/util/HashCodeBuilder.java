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

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public class HashCodeBuilder<T> {

    protected final T target;
    protected int result = 1;

    public HashCodeBuilder(T target) {
        this.target = target;
    }

    public HashCodeBuilder<T> add(Object... values) {
        for (final Object value : values) {
            if (value != null) {
                result = result * 31 + value.hashCode();
            }
        }
        return this;
    }

    public HashCodeBuilder<T> add(Function<T, Object> valueFunc) {
        final Object value = valueFunc.apply(target);
        if (value != null) {
            result = result * 31 + value.hashCode();
        }
        return this;
    }

    public HashCodeBuilder<T> $(Object... values) {
        return add(values);
    }

    public HashCodeBuilder<T> $(Function<T, Object> valueFunc) {
        return add(valueFunc);
    }

    public int build() {
        return result;
    }

    public int build(int previous) {
        return 31 * previous + result;
    }
}
