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

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataUtil {

    public static int hashCode(Entry<?> entry) {
        return Objects.hash(entry.getKey(), entry.getValue());
    }

    public static boolean equals(Entry<?> o1, Object o2) {
        return o2 instanceof Entry && Arrays.deepEquals(
                new Object[]{o1.getKey(), ((Entry) o2).getValue()},
                new Object[]{((Entry) o2).getKey(), ((Entry) o2).getValue()});
    }

    public static int hashCode(Value<?> value) {
        return Objects.hashCode(value.getValue());
    }

    public static boolean equals(Value<?> o1, Object o2) {
        return o2 instanceof Value && Objects.deepEquals(o1.getValue(), ((Value) o2).getValue());
    }
}
