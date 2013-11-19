/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.io;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleSafeResult<T> implements SafeResult<T> {

    private final T value;
    private final List<Throwable> errors;

    public SimpleSafeResult(T value, List<Throwable> errors) {
        this.value = value;
        this.errors = errors;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public List<Throwable> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (value instanceof Map) {
            sb.append("map(").append(((Map) value).size()).append(") ");
        } else if (value instanceof Collection) {
            sb.append("collection(").append(((Collection) value).size()).append(") ");
        } else if (value == null) {
            sb.append("null ");
        } else if (value.getClass().isArray()) {
            sb.append("array(").append(Array.getLength(value)).append(") ");
        } else {
            sb.append(value).append(' ');
        }
        sb.append('(').append(errors.size()).append(" errors)");
        return sb.toString();
    }
}
