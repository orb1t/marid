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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TreeObject {

    TreeObject parent();

    Collection<? extends TreeObject> children();

    String path();

    String name();

    List<String> listPath();

    String[] arrayPath();

    Object get(String name);

    <T> T get(Class<T> type, String key);

    <T> T get(Class<T> type, String key, T def);

    Object getAt(String name);

    Object put(String name, Object value);

    void putAt(String name, Object value);

    Set<String> keySet();

    TreeObject object(String... path);

    TreeObject object(List<String> path);

    ConcurrentMap<String, Object> vars();

    TreeObject getRoot();
}
