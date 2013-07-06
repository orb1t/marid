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
import javax.xml.bind.annotation.XmlTransient;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "map")
@XmlSeeAlso({
        AbstractEntry.class,
        IntArrayEntry.class
})
public class MapValue extends AbstractValue<Map<String, Object>> {

    @XmlTransient
    private final Map<String, Object> value;

    public MapValue() {
        value = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public MapValue(Map map) {
        value = new LinkedHashMap<>(map.size());
        for (Object oe : map.entrySet()) {
            Map.Entry e = (Map.Entry) oe;
            if (e.getKey() != null) {
                value.put(e.getKey().toString(), e.getValue());
            }
        }
    }

    @Override
    public Map<String, Object> getValue() {
        return value;
    }

    @XmlAnyElement(lax = true)
    private Entry[] getEntries() {
        return toEntries(value);
    }

    public void setEntries(Entry[] entries) {
        value.putAll(toMap(entries));
    }

    static Entry[] toEntries(Map map) {
        List<Entry> entryList = new LinkedList<>();
        for (Object o : map.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            if (e.getKey() != null) {
                Object v = e.getValue();
                if (v instanceof Map) {
                    entryList.add(new MapEntry(e.getKey().toString(), (Map) v));
                } else if (v instanceof Integer) {
                    entryList.add(new IntEntry(e.getKey().toString(), (Integer) v));
                } else if (v instanceof Long) {
                    entryList.add(new LongEntry(e.getKey().toString(), (Long) v));
                } else if (v instanceof int[]) {
                    entryList.add(new IntArrayEntry(e.getKey().toString(), (int[]) v));
                }
            }
        }
        return entryList.toArray(new Entry[entryList.size()]);
    }

    static Map<String, Object> toMap(Entry[] entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Entry e : entries) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}
