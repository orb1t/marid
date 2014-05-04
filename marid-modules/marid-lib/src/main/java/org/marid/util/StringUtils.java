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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.Character.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class StringUtils {

    public static String capitalize(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String decapitalize(String text) {
        return text.isEmpty() ? text : text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    public static String repeated(char symbol, int times) {
        char[] buf = new char[times];
        Arrays.fill(buf, symbol);
        return String.valueOf(buf);
    }

    public static String repeated(String str, int times) {
        int n = str.length();
        char[] buf = new char[n * times];
        for (int i = 0; i < buf.length; i += n) {
            str.getChars(0, n, buf, i);
        }
        return new String(buf);
    }

    public static String repeated(Object obj, int times) {
        return repeated(String.valueOf(obj), times);
    }

    public static String delimited(char delimiter, String... array) {
        int count = 0;
        for (String s : array) {
            if (count > 0) {
                count++;
            }
            count += s == null ? 4 : s.length();
        }
        char[] buf = new char[count];
        count = 0;
        for (String s : array) {
            if (count > 0) {
                buf[count++] = delimiter;
            }
            if (s == null) {
                s = "null";
            }
            s.getChars(0, s.length(), buf, count);
            count += s.length();
        }
        return String.valueOf(buf);
    }

    public static String delimited(String delimiter, String... array) {
        int n = delimiter.length();
        int count = 0;
        for (String s : array) {
            if (count > 0) {
                count += n;
            }
            count += s == null ? 4 : s.length();
        }
        char[] buf = new char[count];
        count = 0;
        for (String s : array) {
            if (count > 0) {
                delimiter.getChars(0, n, buf, count);
                count += n;
            }
            if (s == null) {
                s = "null";
            }
            s.getChars(0, s.length(), buf, count);
            count += s.length();
        }
        return String.valueOf(buf);
    }

    public static String delimited(char delimiter, Object... array) {
        String[] a = new String[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = String.valueOf(array[i]);
        }
        int count = 0;
        for (String s : a) {
            if (count > 0) {
                count++;
            }
            count += s.length();
        }
        char[] buf = new char[count];
        count = 0;
        for (String s : a) {
            if (count > 0) {
                buf[count++] = delimiter;
            }
            s.getChars(0, s.length(), buf, count);
            count += s.length();
        }
        return String.valueOf(buf);
    }

    public static String delimited(String delimiter, Object... array) {
        String[] a = new String[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = String.valueOf(array[i]);
        }
        int n = delimiter.length();
        int count = 0;
        for (String s : a) {
            if (count > 0) {
                count += n;
            }
            count += s.length();
        }
        char[] buf = new char[count];
        count = 0;
        for (String s : a) {
            if (count > 0) {
                delimiter.getChars(0, n, buf, count);
                count += n;
            }
            s.getChars(0, s.length(), buf, count);
            count += s.length();
        }
        return String.valueOf(buf);
    }

    public static int atoi(byte[] buf, int off, int len) {
        return Integer.parseInt(new String(buf, off, len, StandardCharsets.ISO_8859_1));
    }

    public static String camelToText(String camel) {
        final StringBuilder builder = new StringBuilder(camel.length());
        final int[] cps = camel.trim().codePoints().toArray();
        for (int i = 0; i < cps.length; i++) {
            final int cp = i == 0 ? toUpperCase(cps[i]) : cps[i];
            builder.appendCodePoint(cp);
            if (isUpperCase(cp) && i < cps.length - 2 && isUpperCase(cps[i + 1]) && isLowerCase(cps[i + 2])) {
                builder.append(' ');
            } else if (isLowerCase(cp) && i < cps.length - 1 && isUpperCase(cps[i + 1])) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    public static String constantToText(String constant) {
        final String[] parts = constant.split("_");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].toLowerCase();
            if (i == 0) {
                parts[i] = capitalize(parts[i]);
            }
        }
        return String.join(" ", parts);
    }
}
