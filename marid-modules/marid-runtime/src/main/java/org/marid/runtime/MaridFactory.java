/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime;

public interface MaridFactory {

  static int int32(String value) {
    return Integer.parseInt(value);
  }

  static int int32h(String value) {
    return Integer.parseInt(value, 16);
  }

  static int uint32(String value) {
    return Integer.parseUnsignedInt(value);
  }

  static int uint32h(String value) {
    return Integer.parseUnsignedInt(value, 16);
  }

  static long int64(String value) {
    return Long.parseLong(value);
  }

  static long uint64(String value) {
    return Long.parseUnsignedLong(value);
  }

  static double float64(String value) {
    return Double.parseDouble(value);
  }

  static float float32(String value) {
    return Float.parseFloat(value);
  }

  static short int16(String value) {
    return Short.parseShort(value);
  }

  static short int16h(String value) {
    return Short.parseShort(value, 16);
  }

  static byte int8(String value) {
    return Byte.parseByte(value);
  }

  static byte int8h(String value) {
    return Byte.parseByte(value, 16);
  }

  static String string(String value) {
    return value;
  }
}
