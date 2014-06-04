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

package org.marid.collections;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ImmutableArrayMap<K, V> extends AbstractMap<K, V> {

    private final Set<Entry<K, V>> entrySet;

    public ImmutableArrayMap(Collection<K> keys, Collection<V> values) {
        final ArrayList<Entry<K, V>> list = new ArrayList<>(Math.min(keys.size(), values.size()));
        final Iterator<K> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();
        while (ki.hasNext() && vi.hasNext()) {
            list.add(new SimpleImmutableEntry<>(ki.next(), vi.next()));
        }
        entrySet = new ImmutableArraySet<>(list);
    }

    public ImmutableArrayMap() {
        entrySet = Collections.emptySet();
    }

    public ImmutableArrayMap(K k1, V v1) {
        entrySet = Collections.singleton(new SimpleImmutableEntry<>(k1, v1));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2) {
        this(Arrays.asList(k1, k2), Arrays.asList(v1, v2));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        this(Arrays.asList(k1, k2, k3), Arrays.asList(v1, v2, v3));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        this(Arrays.asList(k1, k2, k3, k4), Arrays.asList(v1, v2, v3, v4));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        this(Arrays.asList(k1, k2, k3, k4, k5), Arrays.asList(v1, v2, v3, v4, v5));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        this(Arrays.asList(k1, k2, k3, k4, k5, k6), Arrays.asList(v1, v2, v3, v4, v5, v6));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        this(Arrays.asList(k1, k2, k3, k4, k5, k6, k7), Arrays.asList(v1, v2, v3, v4, v5, v6, v7));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        this(Arrays.asList(k1, k2, k3, k4, k5, k6, k7, k8), Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        this(Arrays.asList(k1, k2, k3, k4, k5, k6, k7, k8, k9), Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8, v9));
    }

    public ImmutableArrayMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        this(Arrays.asList(k1, k2, k3, k4, k5, k6, k7, k8, k9, k10), Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }
}
