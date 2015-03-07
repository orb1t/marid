/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.dyn;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class Casting {

    public static <T> T castTo(Class<T> type, Object object) {
        if (object instanceof Number) {
            return DefaultGroovyMethods.asType((Number) object, type);
        } else if (object instanceof Map) {
            return DefaultGroovyMethods.asType((Map) object, type);
        } else if (object instanceof Closure) {
            return DefaultGroovyMethods.asType((Closure) object, type);
        } else if (object instanceof Collection) {
            return DefaultGroovyMethods.asType((Collection) object, type);
        } else if (object instanceof Object[]) {
            return DefaultGroovyMethods.asType((Object[]) object, type);
        } else {
            return DefaultGroovyMethods.asType(object, type);
        }
    }

    public static <T> T mapv(Map map, Object key, Class<T> type) {
        return castTo(type, map.get(key));
    }

    public static <T> T mapv(Map map, Object key, Class<T> type, Supplier<? extends T> supplier) {
        final Object v = map.get(key);
        return v == null ? supplier.get() : castTo(type, v);
    }
}
