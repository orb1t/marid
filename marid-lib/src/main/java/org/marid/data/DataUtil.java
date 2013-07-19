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

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataUtil {

    public static int hashCode(Entry<?> entry) {
        return hash(entry.getKey(), entry.getValue());
    }

    public static boolean equals(Entry<?> o1, Object o2) {
        return o2 instanceof Entry && equals(
                new Object[]{o1.getKey(), ((Entry) o2).getValue()},
                new Object[]{((Entry) o2).getKey(), ((Entry) o2).getValue()});
    }

    public static int hashCode(Value<?> value) {
        return hash(value.getValue());
    }

    public static boolean equals(Value<?> o1, Object o2) {
        return o2 instanceof Value && equals(o1.getValue(), ((Value) o2).getValue());
    }

    public static String toString(Entry<?> entry) {
        Object v = entry.getValue();
        if (v instanceof Map) {
            v = prepareToString((Map) v);
        } else if (v instanceof Collection) {
            v = prepareToString((Collection) v);
        }
        return Arrays.deepToString(new Object[] {entry.getKey(), v});
    }

    public static String toString(Value<?> value) {
        Object v = value.getValue();
        if (v instanceof Map) {
            v = prepareToString((Map) v);
        } else if (v instanceof Collection) {
            v = prepareToString((Collection) v);
        } else if (v != null && v.getClass().isArray()) {
            v = arrayToString(v);
        }
        return String.valueOf(v);
    }

    public static Map<String, Object> prepareToString(Map map) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (Object o : map.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String key = Objects.toString(e.getKey(), null);
            Object value = e.getValue();
            if (value != null) {
                if (value instanceof Map) {
                    value = prepareToString((Map) value);
                } else if (value instanceof Collection) {
                    value = prepareToString((Collection) value);
                } else if (value.getClass().isArray()) {
                    value = arrayToString(value);
                }
            }
            m.put(key, value);
        }
        return m;
    }

    public static Collection<Object> prepareToString(Collection collection) {
        ArrayList<Object> l = new ArrayList<>(collection.size());
        for (Object o : collection) {
            if (o != null) {
                if (o instanceof Map) {
                    o = prepareToString((Map) o);
                } else if (o instanceof Collection) {
                    o = prepareToString((Collection) o);
                } else if (o.getClass().isArray()) {
                    o = arrayToString(o);
                }
            }
            l.add(o);
        }
        return l;
    }

    public static String arrayToString(Object object) {
        int n = Array.getLength(object);
        String[] array = new String[n];
        for (int i = 0; i < n; i++) {
            Object e = Array.get(object, i);
            array[i] = String.valueOf(e);
        }
        return Arrays.toString(array);
    }

    @SuppressWarnings("unchecked")
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        } else if (o1.getClass().isArray()) {
            if (!o2.getClass().isArray()) {
                return false;
            } else {
                int n = Array.getLength(o1);
                if (Array.getLength(o2) != n) {
                    return false;
                }
                for (int i = 0; i < n; i++) {
                    Object v1 = Array.get(o1, i);
                    Object v2 = Array.get(o2, i);
                    if (!equals(v1, v2)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (o1 instanceof Map) {
            if (!(o2 instanceof Map)) {
                return false;
            } else {
                TreeMap<Object, Object> tm1 = new TreeMap<Object, Object>((Map) o1);
                TreeMap<Object, Object> tm2 = new TreeMap<Object, Object>((Map) o2);
                if (tm1.size() != tm2.size()) {
                    return false;
                } else {
                    Iterator<Map.Entry<Object, Object>> i1 = tm1.entrySet().iterator();
                    Iterator<Map.Entry<Object, Object>> i2 = tm2.entrySet().iterator();
                    while (i1.hasNext()) {
                        Map.Entry<Object, Object> e1 = i1.next();
                        Map.Entry<Object, Object> e2 = i2.next();
                        if (!equals(e1.getKey(), e2.getKey())) {
                            return false;
                        }
                        if (!equals(e1.getValue(), e2.getValue())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (o1 instanceof Set) {
            if (!(o2 instanceof Set)) {
                return false;
            } else {
                TreeSet<Object> ts1 = new TreeSet<Object>((Set) o1);
                TreeSet<Object> ts2 = new TreeSet<Object>((Set) o2);
                if (ts1.size() != ts2.size()) {
                    return false;
                } else {
                    Iterator<Object> i1 = ts1.iterator();
                    Iterator<Object> i2 = ts2.iterator();
                    while (i1.hasNext()) {
                        o1 = i1.next();
                        o2 = i2.next();
                        if (!equals(o1, o2)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (o1 instanceof Collection) {
            if (!(o2 instanceof Collection)) {
                return false;
            } else {
                Collection c1 = (Collection) o1;
                Collection c2 = (Collection) o2;
                if (c1.size() != c2.size()) {
                    return false;
                } else {
                    Iterator i1 = c1.iterator();
                    Iterator i2 = c2.iterator();
                    while (i1.hasNext()) {
                        o1 = i1.next();
                        o2 = i2.next();
                        if (!equals(o1, o2)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else {
            return Objects.equals(o1, o2);
        }
    }

    @SuppressWarnings("unchecked")
    public static int hash(Object object) {
        if (object == null) {
            return 0;
        } else if (object.getClass().isArray()) {
            int hash = 1;
            int n = Array.getLength(object);
            for (int i = 0; i < n; i++) {
                hash = hash * 31 + hash(Array.get(object, i));
            }
            return hash;
        } else if (object instanceof Map) {
            TreeMap<Object, Object> tm = new TreeMap<Object, Object>((Map) object);
            int hash = 1;
            for (Map.Entry<Object, Object> e : tm.entrySet()) {
                hash = hash * 31 + hash(e.hashCode());
                hash = hash * 31 + hash(e.getValue());
            }
            return hash;
        } else if (object instanceof Set) {
            TreeSet<Object> ts = new TreeSet<Object>((Set) object);
            int hash = 1;
            for (Object o : ts) {
                hash = hash * 31 + hash(o);
            }
            return hash;
        } else if (object instanceof Collection) {
            int hash = 1;
            for (Object o : (Collection) object) {
                hash = hash * 31 + hash(o);
            }
            return hash;
        } else {
            return object.hashCode();
        }
    }

    public static int hash(Object... objects) {
        int hash = 1;
        for (Object o : objects) {
            hash = hash * 31 + hash(o);
        }
        return hash;
    }
}
