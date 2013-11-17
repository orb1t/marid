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

    private final T result;
    private final List<Throwable> errors;

    public SimpleSafeResult(T result, List<Throwable> errors) {
        this.result = result;
        this.errors = errors;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public List<Throwable> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (result instanceof Map) {
            sb.append("map(").append(((Map) result).size()).append(") ");
        } else if (result instanceof Collection) {
            sb.append("collection(").append(((Collection) result).size()).append(") ");
        } else if (result == null) {
            sb.append("null ");
        } else if (result.getClass().isArray()) {
            sb.append("array(").append(Array.getLength(result)).append(") ");
        } else {
            sb.append(result).append(' ');
        }
        sb.append('(').append(errors.size()).append(" errors)");
        return sb.toString();
    }
}
