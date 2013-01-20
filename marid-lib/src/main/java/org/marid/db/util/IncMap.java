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
package org.marid.db.util;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.Name;

/**
 * Inc-map implementation.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class IncMap implements Map<String, Long> {

    private final Map<String, Long> d;
    private final long delta;
    private final Map<String, Entry<String, Long>> entryCache = new HashMap<>();

    public IncMap(Map<String, Long> dg, long dt) {
        d = dg;
        delta = dt;
    }

    @Override
    public int size() {
        return d.size();
    }

    @Override
    public boolean isEmpty() {
        return d.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return d.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Long) {
            return d.containsValue(((Long)value) + delta);
        } else {
            return false;
        }
    }

    @Override
    public Long get(Object key) {
        Long val = d.get(key);
        if (val == null) {
            return null;
        } else {
            return val + delta;
        }
    }

    @Override
    public Long put(String key, Long value) {
        return d.put(key, value - delta);
    }

    @Override
    public Long remove(Object key) {
        Long val = d.remove(key);
        if (val == null) {
            return null;
        } else {
            return val + delta;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Long> m) {
        d.putAll(new IncMap(d, -delta));
    }

    @Override
    public void clear() {
        d.clear();
    }

    @Override
    public Set<String> keySet() {
        return d.keySet();
    }

    @Override
    public Collection<Long> values() {
        return new AbstractCollection<Long>() {
            @Override
            public Iterator<Long> iterator() {
                final Iterator<Entry<String, Long>> i = d.entrySet().iterator();
                return new Iterator<Long>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public Long next() {
                        return i.next().getValue() + delta;
                    }

                    @Override
                    public void remove() {
                        i.remove();
                    }
                };
            }

            @Override
            public int size() {
                return d.size();
            }

            @Override
            public boolean isEmpty() {
                return d.isEmpty();
            }

            @Override
            public void clear() {
                d.clear();
            }
        };
    }

    @Override
    public Set<Entry<String, Long>> entrySet() {
       return new AbstractSet<Entry<String, Long>>() {
            @Override
            public Iterator<Entry<String, Long>> iterator() {
                final Iterator<Entry<String, Long>> i = d.entrySet().iterator();
                return new Iterator<Entry<String, Long>>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public Entry<String, Long> next() {
                        Entry<String, Long> e = i.next();
                        String k = e.getKey();
                        Long v = e.getValue() + delta;
                        synchronized(entryCache) {
                            if (entryCache.containsKey(k)) {
                                e = entryCache.get(k);
                                e.setValue(v);
                                return e;
                            } else {
                                e = new SimpleEntry<>(k, v);
                                entryCache.put(k, e);
                                return e;
                            }
                        }
                    }

                    @Override
                    public void remove() {
                        i.remove();
                    }
                };
            }

            @Override
            public boolean add(Entry<String, Long> e) {
                d.put(e.getKey(), e.getValue() - delta);
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends Entry<String, Long>> c) {
                for (Entry<String, Long> e : c) {
                    d.put(e.getKey(), e.getValue() - delta);
                }
                return true;
            }

            @Override
            public int size() {
                return d.size();
            }

            @Override
            public boolean isEmpty() {
                return d.isEmpty();
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (d.isEmpty()) {
            return sb.append('}').toString();
        }
        Iterator<Entry<String, Long>> i = d.entrySet().iterator();
        while (true) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(',').append(' ');
            } else {
                sb.append('}');
                break;
            }
        }
        return sb.toString();
    }
}
