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
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

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
        } else if (Number.class.isAssignableFrom(klass)) {
            return (T) numberCast(klass, v);
        } else if (Character.class == klass) {
            String vs = v.toString();
            if (!vs.isEmpty()) {
                return (T) Character.valueOf(vs.charAt(0));
            } else {
                throw new IllegalArgumentException("Unable to convert empty string to char");
            }
        } else if (Boolean.class == klass) {
            if (v instanceof Number) {
                return (T) Boolean.valueOf(((Number) v).intValue() != 0);
            } else {
                String vb = v.toString();
                return (T) Boolean.valueOf("true".equalsIgnoreCase(vb) || "1".equals(vb));
            }
        } else if (klass.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>)klass, v.toString());
        } else if (Collection.class.isAssignableFrom(klass)) {
            return (T) collectionCast(klass, v);
        } else {
            throw new UnsupportedOperationException("Cannot convert " + v.getClass() + " to " + klass);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection collectionCast(Class<?> klass, Object v) {
        Collection collection;
        if (klass.isInterface()) {
            if (NavigableSet.class == klass || SortedSet.class == klass) {
                collection = new TreeSet();
            } else if (Set.class == klass) {
                collection = new LinkedHashSet();
            } else if (Queue.class == klass || Deque.class == klass) {
                collection = new LinkedList();
            } else if (List.class == klass) {
                collection = new ArrayList();
            } else {
                throw new IllegalArgumentException("Unsupported collection: " + klass);
            }
        } else {
            try {
                collection = (Collection) klass.newInstance();
            } catch (InstantiationException | IllegalAccessException x) {
                throw new IllegalStateException("Unable to create a collection instance", x);
            }
        }
        if (v.getClass().isArray()) {
            int n = Array.getLength(v);
            for (int i = 0; i < n; i++) {
                collection.add(Array.get(v, i));
            }
        } else if (v instanceof Iterable) {
            for (Object e : (Iterable) v) {
                collection.add(e);
            }
        } else {
            throw new IllegalArgumentException("Unable to convert " + v.getClass() + " to collection");
        }
        return collection;
    }

    private Object numberCast(Class<?> klass, Object v) {
        if (v instanceof Number) {
            if (klass == Integer.class) {
                return ((Number) v).intValue();
            } else if (klass == Long.class) {
                return ((Number) v).longValue();
            } else if (klass == Float.class) {
                return ((Number) v).floatValue();
            } else if (klass == Double.class) {
                return ((Number) v).doubleValue();
            } else if (klass == Short.class) {
                return ((Number) v).shortValue();
            } else if (klass == Byte.class) {
                return ((Number) v).byteValue();
            } else if (klass == BigInteger.class) {
                return new BigInteger(v.toString());
            } else if (klass == BigDecimal.class) {
                return new BigDecimal(v.toString());
            }
        }
        try {
            return klass.getConstructor(String.class).newInstance(v.toString());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException x) {
            throw new IllegalStateException("Unable to convert " + v.getClass() + " to " + klass, x);
        }
    }

    private Object arrayCast(Class<?> klass, Object v) {
        if (v == null) {
            return null;
        } else {
            Class<?> vc = v.getClass();
            Class<?> ct = klass.getComponentType();
            if (!ct.isArray()) {
                Object array;
                if (vc.isArray()) {
                    int n = Array.getLength(v);
                    array = Array.newInstance(ct, n);
                    for (int i = 0; i < n; i++) {
                        Array.set(array, i, cast(ct, Array.get(v, i)));
                    }
                } else if (v instanceof Collection) {
                    array = Array.newInstance(ct, ((Collection) v).size());
                    int i = 0;
                    for (Object o : (Collection) v) {
                        Array.set(array, i++, cast(ct, o));
                    }
                } else {
                    throw new IllegalArgumentException("Unable to cast to array from " + vc);
                }
                return array;
            } else {
                int ndim;
                Class<?> cc;
                for (cc = klass, ndim = 0; cc.isArray(); cc = cc.getComponentType()) {
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
                for (int i = 0; i < array.length; i++) {
                    Array.set(result, i, arrayCast(ct, array[i]));
                }
                return result;
            }
        }
    }
}
