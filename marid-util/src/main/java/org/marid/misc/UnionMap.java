/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.misc;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class UnionMap<K, V> extends AbstractMap<K, V> {

    private final List<Map<K, V>> maps;

    public UnionMap(List<Map<K, V>> maps) {
        this.maps = maps;
    }

    @SafeVarargs
    public static <K, V> UnionMap<K, V> of(Map<K, V>... maps) {
        return new UnionMap<>(Arrays.asList(maps));
    }

    @Override
    @Nonnull
    public Set<K> keySet() {
        return maps.stream().flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toSet());
    }

    @Override
    @Nonnull
    public Set<Entry<K, V>> entrySet() {
        final Map<K, V> map = new HashMap<>();
        maps.forEach(map::putAll);
        return map.entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return maps.stream().anyMatch(m -> m.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return maps.stream().anyMatch(m -> m.containsValue(value));
    }

    @Override
    public V get(Object key) {
        return getOptional(key).orElse(null);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return getOptional(key).orElse(defaultValue);
    }

    private Optional<V> getOptional(Object key) {
        return maps.stream().map(m -> m.get(key)).filter(Objects::nonNull).findAny();
    }

    @Override
    public int size() {
        return (int) maps.stream().flatMap(m -> m.keySet().stream()).distinct().count();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        final Set<K> keys = new HashSet<>();
        maps.forEach(m -> m.forEach((k, v) -> {
            if (keys.add(k)) {
                action.accept(k, v);
            }
        }));
    }
}
