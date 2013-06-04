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

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Parameterized extends Map<String, Object> {

    public <T> T get(Class<T> klass, String key);

    public <T> T get(Class<T> klass, String key, T def);

    public int getInt(String key, int def);

    public short getShort(String key, short def);

    public boolean getBoolean(String key, boolean def);

    public long getLong(String key, long def);

    public float getFloat(String key, float def);

    public double getDouble(String key, double def);

    public char getChar(String key, char def);

    public byte getByte(String key, byte def);

    public String getString(String key, String def);
}
