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

package org.marid.service.proto.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class NestedMap implements Map<String, Object> {

    private final NestedMap parent;
    private final Map<String, Object> current = new HashMap<>();

    public NestedMap(@Nonnull NestedMap parent) {
        this.parent = parent;
    }

    public NestedMap() {
        this.parent = null;
    }

    @Nonnull
    private NestedMap getRoot() {
        for (NestedMap map = this; ; map = map.parent) {
            if (map.parent == null) {
                return map;
            }
        }
    }

    @Override
    public int size() {
        synchronized (getRoot()) {
            if (parent == null) {
                return current.size();
            } else {
                int size = parent.size();
                for (final Map.Entry<String, Object> e : current.entrySet()) {
                    if (!parent.containsKey(e.getKey())) {
                        size++;
                    }
                }
                return size;
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (getRoot()) {
            return current.containsKey(key) || parent != null && parent.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (getRoot()) {
            return current.containsValue(value) || parent != null && parent.containsValue(value);
        }
    }

    @Override
    public Object get(Object key) {
        synchronized (getRoot()) {
            final Object v = current.get(key);
            return v == null ? (parent == null ? null : parent.get(key)) : v;
        }
    }

    @Override
    public Object put(String key, Object value) {
        synchronized (getRoot()) {
            return value == null ? current.remove(key) : current.put(key, value);
        }
    }

    @Override
    public Object remove(Object key) {
        synchronized (getRoot()) {
            return current.remove(key);
        }
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ?> m) {
        synchronized (getRoot()) {
            for (final Map.Entry<? extends String, ?> e : m.entrySet()) {
                if (e.getValue() == null) {
                    current.remove(e.getKey());
                } else {
                    current.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    @Override
    public void clear() {
        synchronized (getRoot()) {
            current.clear();
        }
    }

    @Nonnull
    @Override
    public Set<String> keySet() {
        final Set<String> set = new HashSet<>(current.size() + (parent != null ? parent.size() : 0));
        synchronized (getRoot()) {
            set.addAll(current.keySet());
            if (parent != null) {
                set.addAll(parent.keySet());
            }
        }
        return set;
    }

    @Nonnull
    @Override
    public Collection<Object> values() {
        final Map<String, Object> map = new LinkedHashMap<>(current.size() + (parent != null ? parent.size() : 0));
        synchronized (getRoot()) {
            map.putAll(current);
            if (parent != null) {
                map.putAll(parent);
            }
        }
        return map.values();
    }

    @Nonnull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        final Map<String, Object> map = new LinkedHashMap<>(current.size() + (parent != null ? parent.size() : 0));
        synchronized (getRoot()) {
            map.putAll(current);
            if (parent != null) {
                map.putAll(parent);
            }
        }
        return map.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        synchronized (getRoot()) {
            current.forEach(action);
            if (parent != null) {
                parent.forEach((k, v) -> {
                    if (!current.containsKey(k)) {
                        action.accept(k, v);
                    }
                });
            }
        }
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        synchronized (getRoot()) {
            return current.replace(key, oldValue, newValue);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        synchronized (getRoot()) {
            return current.remove(key, value);
        }
    }

    @Override
    public Object replace(String key, Object value) {
        synchronized (getRoot()) {
            return current.replace(key, value);
        }
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        synchronized (getRoot()) {
            current.replaceAll(function);
        }
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        synchronized (getRoot()) {
            return Map.super.compute(key, remappingFunction);
        }
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        synchronized (getRoot()) {
            return Map.super.computeIfAbsent(key, mappingFunction);
        }
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        synchronized (getRoot()) {
            return Map.super.computeIfPresent(key, remappingFunction);
        }
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        final Object v = get(key);
        return v == null ? defaultValue : v;
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        synchronized (getRoot()) {
            return Map.super.putIfAbsent(key, value);
        }
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        synchronized (getRoot()) {
            return Map.super.merge(key, value, remappingFunction);
        }
    }

    @Override
    public int hashCode() {
        synchronized (getRoot()) {
            if (parent != null) {
                return current.hashCode() ^ parent.hashCode();
            } else {
                return current.hashCode();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NestedMap) {
            final NestedMap that = (NestedMap) obj;
            return that.current.equals(this.current) && that.parent == this.parent;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
