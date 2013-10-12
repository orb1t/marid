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

package org.marid.typecast;

import groovy.lang.Closure;
import groovy.lang.GString;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConfigurableObject implements Configurable {

    protected final TreeMap<String, Object> parameters = new TreeMap<>();

    @Override
    public Object get(String key) {
        return parameters.get(key);
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(parameters.keySet());
    }

    @Override
    public <T> T get(Class<T> klass, String key) {
        Object v = parameters.get(key);
        if (v instanceof Number) {
            return DefaultGroovyMethods.asType((Number) v, klass);
        } else if (v instanceof Collection) {
            return DefaultGroovyMethods.asType((Collection) v, klass);
        } else if (v instanceof Map) {
            return DefaultGroovyMethods.asType((Map) v, klass);
        } else if (v instanceof Object[]) {
            return DefaultGroovyMethods.asType((Object[]) v, klass);
        } else if (v instanceof String) {
            return StringGroovyMethods.asType((String) v, klass);
        } else if (v instanceof CharSequence) {
            return StringGroovyMethods.asType((CharSequence) v, klass);
        } else if (v instanceof Closure) {
            return DefaultGroovyMethods.asType((Closure) v, klass);
        } else if (v instanceof GString) {
            return StringGroovyMethods.asType((GString) v, klass);
        } else {
            return DefaultGroovyMethods.asType(v, klass);
        }
    }

    @Override
    public <T> T get(Class<T> klass, String key, T def) {
        T value = get(klass, key);
        return value == null ? def : value;
    }

    @Override
    public int getInt(String key, int def) {
        return get(Integer.class, key, def);
    }

    @Override
    public short getShort(String key, short def) {
        return get(Short.class, key, def);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return get(Boolean.class, key, def);
    }

    @Override
    public long getLong(String key, long def) {
        return get(Long.class, key, def);
    }

    @Override
    public float getFloat(String key, float def) {
        return get(Float.class, key, def);
    }

    @Override
    public double getDouble(String key, double def) {
        return get(Double.class, key, def);
    }

    @Override
    public char getChar(String key, char def) {
        return get(Character.class, key, def);
    }

    @Override
    public byte getByte(String key, byte def) {
        return get(Byte.class, key, def);
    }

    @Override
    public String getString(String key, String def) {
        return get(String.class, key, def);
    }
}
