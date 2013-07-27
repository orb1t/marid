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

package org.marid.util;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * @author Dmitry Ovchinnikov
 */
public class Builder {

    @Override
    public boolean equals(Object that) {
        if (getClass() != that.getClass()) {
            return false;
        } else {
            LinkedList<Object> l1 = new LinkedList<>();
            LinkedList<Object> l2 = new LinkedList<>();
            for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    f.setAccessible(true);
                    try {
                        l1.add(f.get(this));
                        l2.add(f.get(that));
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException(x);
                    }
                }
            }
            return l1.equals(l2);
        }
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    Object o = f.get(this);
                    h = h * 31 + (o == null ? 0 : o.hashCode());
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        return h;
    }

    @Override
    public String toString() {
        boolean first = true;
        StringBuilder b = new StringBuilder(getClass().getSimpleName());
        b.append('{');
        for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    if (first) {
                        first = false;
                    } else {
                        b.append(',');
                    }
                    b.append(f.getName());
                    b.append('=');
                    b.append(f.get(this));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        b.append('}');
        return b.toString();
    }
}
