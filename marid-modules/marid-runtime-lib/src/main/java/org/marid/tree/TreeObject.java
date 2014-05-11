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

package org.marid.tree;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TreeObject {

    TreeObject parent();

    Collection<? extends TreeObject> children();

    String path();

    String name();

    default List<String> listPath() {
        return Arrays.asList(arrayPath());
    }

    String[] arrayPath();

    Object get(String name);

    <T> T get(Class<T> type, String key);

    default <T> T get(Class<T> type, String key, T def) {
        final T value = get(type, key);
        return value == null ? def : value;
    }

    default Object getAt(String name) {
        return get(name);
    }

    Object put(String name, Object value);

    default void putAt(String name, Object value) {
        put(name, value);
    }

    Set<String> keySet();

    default TreeObject object(String... path) {
        return object(Arrays.asList(path));
    }

    TreeObject object(List<String> path);

    ConcurrentMap<String, Object> vars();

    TreeObject getRoot();
}
