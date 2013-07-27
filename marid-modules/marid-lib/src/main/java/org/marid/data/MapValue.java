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
import java.util.*;

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
            if (e.getKey() == null) {
                continue;
            }
            Object v = e.getValue();
            if (v instanceof Map) {
                entryList.add(new MapEntry(e.getKey().toString(), (Map) v));
            } else if (v instanceof Collection) {
            } else if (v.getClass().isArray()) {
                if (v.getClass().getComponentType().isPrimitive()) {
                    if (v instanceof int[]) {
                        entryList.add(new IntArrayEntry(e.getKey().toString(), (int[]) v));
                    }
                }
            } else {
                switch (v.getClass().getName()) {
                    case "java.lang.Boolean":
                        break;
                    case "java.lang.Integer":
                        entryList.add(new IntEntry(e.getKey().toString(), (Integer) v));
                        break;
                    case "java.lang.Long":
                        entryList.add(new LongEntry(e.getKey().toString(), (Long) v));
                        break;
                    case "java.lang.Float":
                        entryList.add(new FloatEntry(e.getKey().toString(), (Float) v));
                        break;
                    case "java.lang.Double":
                        entryList.add(new DoubleEntry(e.getKey().toString(), (Double) v));
                        break;
                    case "java.lang.Short":
                        entryList.add(new ShortEntry(e.getKey().toString(), (Short) v));
                        break;
                    case "java.lang.Byte":
                        entryList.add(new ByteEntry(e.getKey().toString(), (Byte) v));
                        break;
                    case "java.lang.Void":
                        entryList.add(new VoidEntry(e.getKey().toString()));
                        break;
                    case "java.lang.Character":
                        break;
                    case "java.lang.String":
                        break;
                    case "java.util.Date":
                        break;
                    case "java.sql.Date":
                        break;
                    case "java.sql.Time":
                        break;
                    case "java.sql.Timestamp":
                        break;
                    case "java.util.TimeZone":
                        break;
                    case "java.util.Locale":
                        break;
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
