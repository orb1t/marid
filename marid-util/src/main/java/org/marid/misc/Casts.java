/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.misc;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Casts {

    @SuppressWarnings("unchecked")
    static <T> T cast(@Nullable Object object) {
        return (T) object;
    }

    static Class<?> primitiveType(String type) {
        switch (type) {
            case "":
                return null;
            case "int":
                return int.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "byte":
                return byte.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            case "long":
                return long.class;
            default:
                return null;
        }
    }

    static boolean pBool(Properties properties, String name, boolean defaultValue) {
        final Object val = properties.get(name);
        if (val instanceof Boolean) {
            return ((Boolean) val);
        } else if (val == null) {
            return defaultValue;
        } else {
            switch (val.toString().toLowerCase()) {
                case "0":
                case "false":
                case "no":
                    return false;
                case "1":
                case "true":
                case "yes":
                    return true;
                default:
                    throw new IllegalArgumentException("Illegal boolean value " + val);
            }
        }
    }

    static int pInt(Properties properties, String name, int defaultValue) {
        final Object val = properties.get(name);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(val.toString());
        }
    }

    static long pLong(Properties properties, String name, long defaultValue) {
        final Object val = properties.get(name);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val == null) {
            return defaultValue;
        } else {
            return Long.parseLong(val.toString());
        }
    }

    static Duration pDur(Properties properties, String name, Duration defaultValue) {
        final Object val = properties.get(name);
        if (val instanceof Number) {
            return Duration.ofSeconds(((Number) val).longValue());
        } else if (val == null) {
            return defaultValue;
        } else {
            return Duration.parse(val.toString());
        }
    }
}
