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
package org.marid.object;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Marid abstract object.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractMaridObject implements MaridObject {

    protected final AbstractMaridObject parent;
    protected final Map source;
    protected final String id;
    protected final ConcurrentMap<String, Object> params;

    /**
     * Constructs the abstract object.
     *
     * @param i Object ID.
     * @param p Parent object.
     * @param map Parameter map.
     */
    public AbstractMaridObject(String i, AbstractMaridObject p, Map map) {
        id = i;
        parent = p;
        source = map;
        params = new ConcurrentSkipListMap<>();
        for (Object oe : map.entrySet()) {
            Map.Entry e = (Map.Entry) oe;
            if (e.getKey() instanceof String) {
                params.put((String) e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public AbstractMaridObject getParent() {
        return parent;
    }

    @Override
    public Map getSource() {
        return source;
    }

    @Override
    public abstract AbstractMaridObject clone();

    /**
     * Get children map (modifiable). By default the empty map will bre
     * returned. This method must be overrided to provide a custom behaviour.
     *
     * @return Children map.
     * @since 1.0
     */
    protected Map<String, ? extends List<? extends AbstractMaridObject>> childrenMap() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, ? extends List<? extends AbstractMaridObject>> getChildrenMap() {
        return Collections.unmodifiableMap(childrenMap());
    }

    @Override
    public String getMemId() {
        return Integer.toHexString(System.identityHashCode(this));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPath() {
        StringBuilder sb = new StringBuilder(id);
        for (AbstractMaridObject o = parent; o != null; o = o.parent) {
            sb.insert(0, '/');
            sb.insert(0, o.id);
        }
        sb.insert(0, '/');
        return sb.toString();
    }

    @Override
    public String getPath(Class<? extends MaridObject> c) {
        if (c.isInstance(this)) {
            return ".";
        } else {
            StringBuilder sb = new StringBuilder(id);
            for (AbstractMaridObject o = parent;
                    o != null && !c.isInstance(o); o = o.parent) {
                sb.insert(0, '/');
                sb.insert(0, o.id);
            }
            return sb.toString();
        }
    }

    @Override
    public String getPath(MaridObject bo) {
        if (bo == this || bo.getPath().equals(getPath())) {
            return ".";
        } else {
            StringBuilder sb = new StringBuilder(id);
            for (AbstractMaridObject o = parent; o != null && o != this
                    && !bo.getPath().equals(o.getPath()); o = o.parent) {
                sb.insert(0, '/');
                sb.insert(0, o.id);
            }
            return sb.toString();
        }
    }

    @Override
    public MaridObject getRoot() {
        for (AbstractMaridObject o = this;; o = o.parent) {
            if (o.parent == null) {
                return o;
            }
        }
    }

    @Override
    public <T extends MaridObject> T getRoot(Class<T> c) {
        for (T o = (T) this;; o = (T) o.getParent()) {
            if (o.getParent() == null || c.isInstance(o)) {
                return o;
            }
        }
    }

    @Override
    public boolean containsKey(String key) {
        return params.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return params.get(key);
    }

    @Override
    public Object get(String key, Object def) {
        Object o = get(key);
        return o != null ? o : def;
    }

    @Override
    public Object put(String key, Object val) {
        return val == null ? params.remove(key) : params.put(key, val);
    }

    @Override
    public int getInt(String key, int def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            return Integer.decode(o.toString());
        }
    }

    @Override
    public long getLong(String key, long def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).longValue();
        } else {
            return Long.decode(o.toString());
        }
    }

    @Override
    public byte getByte(String key, byte def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).byteValue();
        } else {
            return Byte.decode(o.toString());
        }
    }

    @Override
    public short getShort(String key, short def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).shortValue();
        } else {
            return Short.decode(o.toString());
        }
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        } else {
            return Boolean.parseBoolean(o.toString());
        }
    }

    @Override
    public float getFloat(String key, float def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else {
            return Float.parseFloat(o.toString());
        }
    }

    @Override
    public double getDouble(String key, double def) {
        Object o = get(key);
        if (o == null) {
            return def;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            return Double.parseDouble(o.toString());
        }
    }

    @Override
    public String getString(String key) {
        Object o = get(key);
        return o == null ? null : o.toString();
    }

    @Override
    public String getString(String key, String def) {
        Object o = get(key);
        return o == null ? def : o.toString();
    }

    @Override
    public Date getDate(String key, Date def) {
        Object o = get(key);
        return o == null ? def
                : o instanceof Date ? (Date) o
                : o instanceof Number ? new Date(((Number) o).longValue())
                : Timestamp.valueOf(o.toString());
    }

    @Override
    public boolean containsObject(String id) {
        for (Map.Entry<String, ? extends List<? extends AbstractMaridObject>> e :
                childrenMap().entrySet()) {
            for (AbstractMaridObject o : e.getValue()) {
                if (id.equals(o.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsObject(String group, String id) {
        if (childrenMap().containsKey(group)) {
            for (AbstractMaridObject o : childrenMap().get(group)) {
                if (id.equals(o.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(id);
        sb.append('(');
        for (Map.Entry<String, ? extends List<? extends AbstractMaridObject>> e :
                childrenMap().entrySet()) {
            sb.append(e.getKey());
            sb.append(':');
            sb.append(e.getValue().size());
            sb.append(',');
        }
        sb.append(params.keySet());
        sb.append(')');
        return sb.toString();
    }
}
