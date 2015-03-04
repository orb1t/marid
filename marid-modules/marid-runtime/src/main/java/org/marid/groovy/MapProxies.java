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

package org.marid.groovy;

import groovy.lang.Closure;
import org.marid.pref.PrefCodecs;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.marid.util.Utils.currentClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class MapProxies {

    private static final MethodHandles.Lookup LOOKUP;

    static {
        try {
            final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            LOOKUP = (MethodHandles.Lookup) field.get(null);
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T newInstance(Class<T> type, Map map) {
        return type.cast(newProxyInstance(currentClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            final Object value = map.get(method.getName());
            if (value instanceof Closure) {
                return ((Closure) value).call(args);
            } else if (value != null) {
                return PrefCodecs.castTo(value, method.getReturnType());
            } else if (method.isDefault()) {
                return LOOKUP.unreflectSpecial(method, method.getDeclaringClass())
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            } else {
                return null;
            }
        }));
    }
}
