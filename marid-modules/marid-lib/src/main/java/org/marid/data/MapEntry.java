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

package org.marid.data;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "map-entry")
@XmlSeeAlso({
        AbstractEntry.class,
        IntArrayEntry.class
})
public class MapEntry extends AbstractEntry<Map<String, Object>> {

    private final Map<String, Object> value;

    public MapEntry() {
        value = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public MapEntry(String key, Map map) {
        super(key);
        value = new LinkedHashMap<>(Collections.checkedMap(map, String.class, Object.class));
    }

    @Override
    public Map<String, Object> getValue() {
        return value;
    }

    @XmlAnyElement(lax = true)
    private Entry[] getEntries() {
        return MapValue.toEntries(value);
    }

    private void setEntries(Entry[] entries) {
        value.putAll(MapValue.toMap(entries));
    }
}
