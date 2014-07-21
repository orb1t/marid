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

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public class CollectionUtils {

    public static final Object[] EMPTY_ARRAY = new Object[0];

    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] array, T elem) {
        int n = array.length;
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), n + 1);
        System.arraycopy(array, 0, result, 0, n);
        result[n] = elem;
        return result;
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public static <T> T concat(T array, Object elem) {
        int n = Array.getLength(array);
        T result = (T) Array.newInstance(array.getClass().getComponentType(), n + 1);
        System.arraycopy(array, 0, result, 0, n);
        Array.set(result, n, elem);
        return result;
    }

    public static <T> int find(T[] array, T elem, int start) {
        for (int i = start; i < array.length; i++) {
            if (Objects.equals(array[i], elem)) {
                return i;
            }
        }
        return -1;
    }

    public static int find(Object array, Object elem, int start) {
        int n = Array.getLength(array);
        for (int i = start; i < n; i++) {
            if (Objects.equals(Array.get(array, i), elem)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int findLast(T[] array, T elem, int start) {
        for (int i = array.length - start - 1; i >=0; i--) {
            if (Objects.equals(array[i], elem)) {
                return i;
            }
        }
        return -1;
    }

    public static int findLast(Object array, Object elem, int start) {
        int n = Array.getLength(array);
        for (int i = n - start - 1; i >= 0; i--) {
            if (Objects.equals(Array.get(array, i), elem)) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static <E> Class<E[]> getArrayType(Class<E> type) {
        return (Class<E[]>) Array.newInstance(type, 0).getClass();
    }

    @SuppressWarnings("unchecked")
    public static <E> IntFunction<E[]> getArrayFunction(Class<E> type) {
        return n -> (E[]) Array.newInstance(type, n);
    }
}
