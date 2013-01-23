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
package org.marid.db.data;

/**
 * Float object data record.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class ObjectDataRecord<T> extends DataRecord<T> {

    private T value;

    /**
     * Constructs the object data record.
     * @param tag Tag.
     * @param ts Timestamp.
     * @param val Value.
     */
    public ObjectDataRecord(String tag, long ts, T val) {
        super(tag, ts);
        value = val;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T val) {
        if (val == null) {
            throw new NullPointerException("Value is null");
        }
        value = val;
    }
}
