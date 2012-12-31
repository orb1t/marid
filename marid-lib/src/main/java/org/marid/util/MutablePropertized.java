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
package org.marid.util;

import java.util.Map;

/**
 * Mutable propertized.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface MutablePropertized extends Propertized {

    /**
     * Removes the entry by key.
     *
     * @param key Entry key.
     * @return Old value.
     */
    public Object remove(String key);

    /**
     * Removes an entry when there is a entry with value.equals(val).
     *
     * @param key Entry key.
     * @param val Old value.
     * @return True if an entry was removed successfully.
     */
    public boolean remove(String key, Object val);

    /**
     * Puts an entry.
     *
     * @param key Entry key.
     * @param val Entry value.
     * @return Old value.
     */
    public Object put(String key, Object val);

    /**
     * Put entire map into properties.
     * @param map Source map.
     */
    public void put(Map<String, Object> map);

    /**
     * Puts an entry if no entries with given key are present.
     *
     * @param key Entry key.
     * @param val Entry value.
     * @return Old value.
     */
    public Object putIfAbsent(String key, Object val);

    /**
     * Replaces the entry value.
     *
     * @param key Entry key.
     * @param val Entry value.
     * @return Old value.
     */
    public Object replace(String key, Object val);

    /**
     * Replaces the entry value if there is an entry with value.equals(old).
     *
     * @param key Entry key.
     * @param old Old value.
     * @param val Entry value.
     * @return Replacing result.
     */
    public boolean replace(String key, Object old, Object val);

    /**
     * Clears all the keys.
     */
    public void clear();
}
