/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.prp;

import java.util.Map;

/**
 * Concurrent mutable propertized object.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class CMPrp extends CPrp implements MutablePrp {

    @Override
    public Object remove(String key) {
        return map.remove(key);
    }

    @Override
    public boolean remove(String key, Object val) {
        return map.remove(key, val);
    }

    @Override
    public Object put(String key, Object val) {
        return val == null ? map.remove(key) : map.put(key, val);
    }

    @Override
    public Object putIfAbsent(String key, Object val) {
        return val == null ? map.remove(key) : map.putIfAbsent(key, val);
    }

    @Override
    public Object replace(String key, Object val) {
        return val == null ? map.remove(key) : map.replace(key, val);
    }

    @Override
    public boolean replace(String key, Object old, Object val) {
        return val == null ? map.remove(key, old) : map.replace(key, old, val);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void put(Map<String, Object> m) {
        map.putAll(m);
    }
}