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
import org.marid.dyn.Casting;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

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
        return type.cast(newProxyInstance(currentClassLoader(), new Class<?>[]{type}, (proxy, m, args) -> {
            final Object value = map.get(m.getName());
            if (value instanceof Closure) {
                if (m.getReturnType() == void.class) {
                    return ((Closure) value).call(args);
                } else if (m.getReturnType().isAnnotationPresent(FunctionalInterface.class)) {
                    return Casting.castTo(m.getReturnType(), value);
                } else {
                    return Casting.castTo(m.getReturnType(), ((Closure) value).call(args));
                }
            } else if (value != null) {
                CHECK:
                if (Map.class == m.getReturnType() && value instanceof Map) {
                    if (!(m.getGenericReturnType() instanceof ParameterizedType)) {
                        break CHECK;
                    }
                    final ParameterizedType pt = (ParameterizedType) m.getGenericReturnType();
                    if (pt.getActualTypeArguments() == null || pt.getActualTypeArguments().length < 2) {
                        break CHECK;
                    }
                    final Type pt1 = pt.getActualTypeArguments()[1];
                    if (pt1 instanceof Class<?> && ((Class<?>) pt1).isInterface()) {
                        return new ProxiedMap((Map) value, (Class<?>) pt1);
                    }
                } else if (m.getReturnType().isInterface() && value instanceof Map) {
                    return newInstance(m.getReturnType(), (Map) value);
                }
                return Casting.castTo(m.getReturnType(), value);
            } else if (m.isDefault()) {
                return LOOKUP.unreflectSpecial(m, m.getDeclaringClass()).bindTo(proxy).invokeWithArguments(args);
            } else {
                return null;
            }
        }));
    }

    private static class ProxiedMap implements Map {

        private final Map delegate;
        private final Class<?> itf;

        private ProxiedMap(Map delegate, Class<?> itf) {
            this.delegate = delegate;
            this.itf = itf;
        }

        @Override
        public Set keySet() {
            return delegate.keySet();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            final Object v = delegate.get(key);
            return v instanceof Map ? newInstance(itf, (Map) v) : v;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object put(Object key, Object value) {
            return delegate.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void putAll(Map m) {
            delegate.putAll(m);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Collection values() {
            final Collection values = delegate.values();
            return new AbstractCollection() {
                @Override
                public Iterator iterator() {
                    final Iterator iterator = values.iterator();
                    return new Iterator() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Object next() {
                            final Object v = iterator.next();
                            return v instanceof Map ? newInstance(itf, (Map) v) : v;
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<Entry> entrySet() {
            final Set<Entry> set = delegate.entrySet();
            return new AbstractSet<Entry>() {
                @Override
                public Iterator<Entry> iterator() {
                    final Iterator<Entry> iterator = set.iterator();
                    return new Iterator<Entry>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Entry next() {
                            final Entry entry = iterator.next();
                            final Object k = entry.getKey();
                            final Object v = entry.getValue();
                            return entry.getValue() instanceof Map
                                    ? new SimpleImmutableEntry<Object, Object>(k, newInstance(itf, (Map) v))
                                    : entry;
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }
            };
        }
    }
}
