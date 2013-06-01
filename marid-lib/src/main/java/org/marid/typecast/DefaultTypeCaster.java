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

package org.marid.typecast;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultTypeCaster extends TypeCaster {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T cast(Class<T> klass, Object v) {
        if (v == null) {
            return null;
        } else if (klass.isInstance(v)) {
            return (T) v;
        } else if (klass == String.class) {
            return (T) String.valueOf(v);
        } else if (klass.isPrimitive()) {
            if (klass == int.class) {
                return (T) cast(Integer.class, v);
            } else if (klass == boolean.class) {
                return (T) cast(Boolean.class, v);
            } else if (klass == long.class) {
                return (T) cast(Long.class, v);
            } else if (klass == double.class) {
                return (T) cast(Double.class, v);
            } else if (klass == float.class) {
                return (T) cast(Float.class, v);
            } else if (klass == short.class) {
                return (T) cast(Short.class, v);
            } else if (klass == byte.class) {
                return (T) cast(Byte.class, v);
            } else if (klass == char.class) {
                return (T) cast(Character.class, v);
            } else if (klass == void.class) {
                return null;
            } else {
                throw new UnsupportedOperationException("Unsupported cast for " + klass);
            }
        } else if (klass.isArray()) {
            return (T) arrayCast(klass, v);
        } else {
            throw new UnsupportedOperationException("Cannot convert " + v.getClass() + " to " + klass);
        }
    }

    private Object arrayCast(Class<?> klass, Object v) {
        if (v == null) {
            return null;
        } else {
            Class<?> vc = v.getClass();
            if (!klass.getComponentType().isArray()) {
                Object array;
                if (vc.isArray()) {
                    int n = Array.getLength(v);
                    array = Array.newInstance(klass, n);
                    for (int i = 0; i < n; i++) {
                        Array.set(array, i, cast(klass, Array.get(v, i)));
                    }
                } else if (v instanceof Collection) {
                    array = Array.newInstance(klass, ((Collection) v).size());
                    int i = 0;
                    for (Object o : (Collection) v) {
                        Array.set(array, i++, cast(klass, o));
                    }
                } else {
                    throw new IllegalArgumentException("Unable to cast to array from " + vc);
                }
                return array;
            } else {
                int ndim;
                Class<?> cc;
                for (cc = klass, ndim = 1; cc.isArray(); cc = cc.getComponentType()) {
                    ndim++;
                }
                Object[] array;
                if (vc.isArray()) {
                    array = new Object[Array.getLength(v)];
                    for (int i = 0; i < array.length; i++) {
                        array[i] = Array.get(v, i);
                    }
                } else if (v instanceof Collection) {
                    array = ((Collection) v).toArray();
                } else {
                    throw new IllegalArgumentException("Unable to cast to array from " + vc);
                }
                int[] dims = new int[ndim];
                dims[0] = array.length;
                Object result = Array.newInstance(cc, dims);
                Class<?> ct = klass.getComponentType();
                for (int i = 0; i < array.length; i++) {
                    Array.set(result, i, arrayCast(ct, array[i]));
                }
                return result;
            }
        }
    }
}
