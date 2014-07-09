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

package org.marid.pref;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * @author Dmitry Ovchinnikov
 */
public class MapBasedPreferences extends AbstractPreferences {

    protected final Map<String, String> map = new TreeMap<>();
    protected final Map<String, MapBasedPreferences> kids = new TreeMap<>();

    protected MapBasedPreferences(MapBasedPreferences parent, String name) {
        super(parent, name);
    }

    public MapBasedPreferences(String name) {
        super(null, name);
    }

    public MapBasedPreferences() {
        this("");
    }

    @Override
    public MapBasedPreferences parent() {
        return (MapBasedPreferences) super.parent();
    }

    @Override
    protected void putSpi(String key, String value) {
        map.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return map.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        map.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        parent().map.remove(name());
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        return map.keySet().toArray(new String[map.size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        return kids.keySet().toArray(new String[kids.size()]);
    }

    @Override
    protected MapBasedPreferences childSpi(String name) {
        final MapBasedPreferences child = new MapBasedPreferences(this, name);
        kids.put(name, child);
        return child;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
    }

    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
