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

import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class ParameterizedObject extends TreeMap<String, Object> implements Parameterized {

    private static final long serialVersionUID = 1319117435693609192L;

    @Override
    public <T> T get(Class<T> klass, String key) {
        return TypeCaster.CASTER.cast(klass, get(key));
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
