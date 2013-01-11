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
package org.marid.db;

import java.io.IOException;

/**
 * Data current storage.
 *
 * @auhor Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface DataCurrentStorage {

    /**
     * Get a double value by tag.
     * @param tag Value tag.
     * @return Double value.
     */
    public double getDouble(String tag) throws IOException;

    /**
     * Get a float value by tag.
     * @param tag Value tag.
     * @return Float value.
     */
    public float getFloat(String tag) throws IOException;

    /**
     * Get an int by tag.
     * @param tag Value tag.
     * @return Integer value.
     * @throws IOException An I/O exception.
     */
    public int getInt(String tag) throws IOException;

    /**
     * Get long value by tag.
     * @param tag Value tag.
     * @return Long value.
     * @throws IOException An I/O exception.
     */
    public long getLong(String tag) throws IOException;
}
