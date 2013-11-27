/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static java.util.Calendar.*;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class DateUtil {

    public static Calendar isoToCalendar(String date) {
        final TimeZone z;
        int l = date.length();
        switch (l) {
            case 28:
            case 24:
            case 21:
            case 18:
            case 15:
                z = getTimeZone(date, true);
                break;
            case 26:
            case 22:
                z = getTimeZone(date, false);
                break;
            default:
                z = TimeZone.getDefault();
                break;
        }
        final Calendar c = new GregorianCalendar(z, Locale.ROOT);
        if (l != 12 && l >= 10) {
            switch (l) {
                case 28:
                case 26:
                case 23:
                    c.set(HOUR_OF_DAY, parseInt(date, 11, 2));
                    c.set(MINUTE, parseInt(date, 14, 2));
                    c.set(SECOND, parseInt(date, 17, 2));
                    c.set(MILLISECOND, parseInt(date, 20, 3));
                    break;
                case 24:
                case 22:
                case 19:
                    c.set(HOUR_OF_DAY, parseInt(date, 11, 2));
                    c.set(MINUTE, parseInt(date, 14, 2));
                    c.set(SECOND, parseInt(date, 17, 2));
                    c.set(MILLISECOND, 0);
                    break;
                case 21:
                case 16:
                    c.set(HOUR_OF_DAY, parseInt(date, 11, 2));
                    c.set(MINUTE, parseInt(date, 14, 2));
                    c.set(SECOND, 0);
                    c.set(MILLISECOND, 0);
                    break;
                case 18:
                case 13:
                    c.set(HOUR_OF_DAY, parseInt(date, 11, 2));
                    c.set(MINUTE, 0);
                    c.set(SECOND, 0);
                    c.set(MILLISECOND, 0);
                    break;
                case 15:
                case 10:
                    break;
                default:
                    throw new IllegalArgumentException(date);
            }
            c.set(YEAR, parseInt(date, 0, 4));
            c.set(MONTH, parseInt(date, 5, 2) - 1);
            c.set(DATE, parseInt(date, 8, 2));
        } else {
            switch (l) {
                case 12:
                    c.set(MILLISECOND, parseInt(date, l - 3, 3));
                    c.set(SECOND, parseInt(date, l - 6, 2));
                    break;
                case 8:
                    c.set(MILLISECOND, 0);
                    c.set(SECOND, parseInt(date, l - 2, 2));
                    break;
                case 5:
                    c.set(MILLISECOND, 0);
                    c.set(SECOND, 0);
                    break;
                default:
                    throw new IllegalArgumentException(date);
            }
            c.set(MINUTE, parseInt(date, 3, 2));
            c.set(HOUR_OF_DAY, parseInt(date, 0, 2));
            c.set(YEAR, 1970);
            c.set(MONTH, 0);
            c.set(DATE, 1);
        }
        return c;
    }

    public static long isoToMillis(String date) {
        return isoToCalendar(date).getTimeInMillis();
    }

    public static Date isoToDate(String date) {
        return isoToCalendar(date).getTime();
    }

    public static Timestamp isoToTimestamp(String date) {
        return new Timestamp(isoToMillis(date));
    }

    public static void iso(long ts, Appendable a, TimeZone tz, boolean ms) throws IOException {
        final Calendar c = Calendar.getInstance(tz, Locale.ROOT);
        c.setTimeInMillis(ts);
        a.append(Integer.toString(c.get(YEAR)));
        a.append('-');
        int n = c.get(MONTH) + 1;
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append('-');
        n = c.get(DATE);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(' ');
        n = c.get(HOUR);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(':');
        n = c.get(MINUTE);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(':');
        n = c.get(SECOND);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        if (ms) {
            a.append('.');
            n = c.get(MILLISECOND);
            if (n < 10) {
                a.append("00");
            } else if (n < 100) {
                a.append('0');
            }
            a.append(Integer.toString(n));
        }
    }

    private static TimeZone getTimeZone(String s, boolean rfc) {
        final int l = s.length();
        if (rfc) {
            String h = s.substring(l - 5, l - 2);
            String m = s.substring(l - 2, l);
            return TimeZone.getTimeZone("GMT" + h + ":" + m);
        } else {
            return TimeZone.getTimeZone(s.substring(l - 3, l));
        }
    }

    private static int parseInt(String s, int off, int len) {
        return Integer.parseInt(s.substring(off, off + len));
    }
}
