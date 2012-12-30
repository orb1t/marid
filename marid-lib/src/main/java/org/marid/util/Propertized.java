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

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Propertized object interface.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Propertized {

    /**
     * Get the object by key.
     *
     * @param key Property key.
     * @param def Default value.
     * @return An object.
     */
    public Object get(String key, Object def);

    /**
     * Get the object by key.
     *
     * @param key Property key.
     * @return An object.
     */
    public Object get(String key);

    /**
     * Get the typed value.
     *
     * @param <T> Value type.
     * @param c Value type class.
     * @param key Property key.
     * @param def Default value.
     * @return Typed value.
     */
    public <T> T get(Class<T> c, String key, T def);

    /**
     * Type-helper class.
     */
    public static final class $ {

        /**
         * Get string value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return String value.
         */
        public static String getString(Object v, String def) {
            if (v == null) {
                return def;
            } else {
                return v.toString();
            }
        }

        /**
         * Get long value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Long value.
         */
        public static long getLong(Object v, long def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).longValue();
            } else {
                return Long.decode(v.toString());
            }
        }

        /**
         * Get int value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Integer value.
         */
        public static int getInt(Object v, int def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).intValue();
            } else {
                return Integer.decode(v.toString());
            }
        }

        /**
         * Get float value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Float value.
         */
        public static float getFloat(Object v, float def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).floatValue();
            } else {
                return Float.parseFloat(v.toString());
            }
        }

        /**
         * Get double value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Double value.
         */
        public static double getDouble(Object v, double def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).doubleValue();
            } else {
                return Double.parseDouble(v.toString());
            }
        }

        /**
         * Get byte value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Byte value.
         */
        public static byte getByte(Object v, byte def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).byteValue();
            } else {
                return Byte.decode(v.toString());
            }
        }

        /**
         * Get short value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Short value.
         */
        public static short getShort(Object v, short def) {
            if (v == null) {
                return def;
            } else if (v instanceof Number) {
                return ((Number) v).shortValue();
            } else {
                return Short.parseShort(v.toString());
            }
        }

        /**
         * Get the bitSet value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return BitSet value.
         */
        public static BitSet getBitSet(Object v, BitSet def) {
            if (v == null) {
                return def;
            } else if (v instanceof byte[]) {
                return BitSet.valueOf((byte[]) v);
            } else if (v instanceof long[]) {
                return BitSet.valueOf((long[]) v);
            } else if (v instanceof ByteBuffer) {
                return BitSet.valueOf((ByteBuffer) v);
            } else if (v instanceof LongBuffer) {
                return BitSet.valueOf((LongBuffer) v);
            } else if (v instanceof Byte) {
                return BitSet.valueOf(new byte[]{(Byte) v});
            } else if (v instanceof Number) {
                ByteBuffer b;
                if (v instanceof Short) {
                    b = ByteBuffer.allocate(2).putShort((Short) v);
                } else if (v instanceof Long) {
                    b = ByteBuffer.allocate(8).putLong((Long) v);
                } else if (v instanceof Integer) {
                    b = ByteBuffer.allocate(4).putInt((Integer) v);
                } else if (v instanceof Float) {
                    b = ByteBuffer.allocate(4).putFloat((Float) v);
                } else if (v instanceof Double) {
                    b = ByteBuffer.allocate(8).putDouble((Double) v);
                } else if (v instanceof BigInteger) {
                    b = ByteBuffer.wrap(((BigInteger) v).toByteArray());
                } else {
                    b = ByteBuffer.wrap(new BigDecimal(
                            v.toString()).unscaledValue().toByteArray());
                }
                return BitSet.valueOf(b);
            } else if (v instanceof InetAddress) {
                return BitSet.valueOf(((InetAddress) v).getAddress());
            } else if (v instanceof UUID) {
                UUID uuid = (UUID) v;
                ByteBuffer b = ByteBuffer.allocate(16);
                b.putLong(uuid.getMostSignificantBits());
                b.putLong(uuid.getLeastSignificantBits());
                return BitSet.valueOf(b);
            } else {
                char[] buf = v.toString().toCharArray();
                ByteBuffer b = ByteBuffer.allocate(2 * buf.length);
                b.asCharBuffer().put(buf);
                return BitSet.valueOf(b);
            }
        }

        /**
         * Get boolean value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Boolean value.
         */
        public static boolean getBool(Object v, boolean def) {
            if (v == null) {
                return def;
            } else if (v instanceof Boolean) {
                return ((Boolean) v).booleanValue();
            } else if (v instanceof Number) {
                return ((Number) v).intValue() == 1;
            } else {
                return Boolean.parseBoolean(v.toString());
            }
        }

        /**
         * Get timeUnit value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return TimeUnit value.
         */
        public static TimeUnit getTimeUnit(Object v, TimeUnit def) {
            if (v == null) {
                return def;
            } else if (v instanceof TimeUnit) {
                return (TimeUnit) v;
            } else {
                return TimeUnit.valueOf(v.toString().toUpperCase());
            }
        }

        /**
         * Get currency value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Currency value.
         */
        public static Currency getCurrency(Object v, Currency def) {
            if (v == null) {
                return def;
            } else if (v instanceof Currency) {
                return (Currency) v;
            } else if (v instanceof Locale) {
                return Currency.getInstance((Locale) v);
            } else {
                return Currency.getInstance(v.toString());
            }
        }

        /**
         * Get locale value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Locale value.
         */
        public static Locale getLocale(Object v, Locale def) {
            if (v == null) {
                return def;
            } else if (v instanceof Locale) {
                return (Locale) v;
            } else if (v instanceof Locale.Category) {
                return Locale.getDefault((Locale.Category) v);
            } else {
                return Locale.forLanguageTag(v.toString());
            }
        }

        /**
         * Get date value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Date value.
         */
        public static Date getDate(Object v, Date def) {
            if (v == null) {
                return def;
            } else if (v instanceof Date) {
                return (Date) v;
            } else if (v instanceof Calendar) {
                return ((Calendar) v).getTime();
            } else if (v instanceof Number) {
                return new Date(((Number) v).longValue());
            } else {
                return DateUtil.isoToDate(v.toString());
            }
        }

        /**
         * Get calendar value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Calendar value.
         */
        public static Calendar getCalendar(Object v, Calendar def) {
            if (v == null) {
                return def;
            } else if (v instanceof Calendar) {
                return (Calendar) v;
            } else if (v instanceof Date) {
                Calendar c = Calendar.getInstance();
                c.setTime((Date) v);
                return c;
            } else if (v instanceof Number) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(((Number) v).longValue());
                return c;
            } else {
                return DateUtil.isoToCalendar(v.toString());
            }
        }

        /**
         * Get calendar value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Calendar value.
         */
        public static Calendar getCalendar(Object v, Date def) {
            if (v == null) {
                Calendar c = Calendar.getInstance();
                c.setTime(def);
                return c;
            } else if (v instanceof Calendar) {
                return (Calendar) v;
            } else if (v instanceof Date) {
                Calendar c = Calendar.getInstance();
                c.setTime((Date) v);
                return c;
            } else if (v instanceof Number) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(((Number) v).longValue());
                return c;
            } else {
                return DateUtil.isoToCalendar(v.toString());
            }
        }

        /**
         * Get calendar value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Calendar value.
         */
        public static Calendar getCalendar(Object v, long def) {
            if (v == null) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(def);
                return c;
            } else if (v instanceof Calendar) {
                return (Calendar) v;
            } else if (v instanceof Date) {
                Calendar c = Calendar.getInstance();
                c.setTime((Date) v);
                return c;
            } else if (v instanceof Number) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(((Number) v).longValue());
                return c;
            } else {
                return DateUtil.isoToCalendar(v.toString());
            }
        }

        /**
         * Get charset value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Charset value.
         */
        public static Charset getCharset(Object v, Charset def) {
            if (v == null) {
                return def;
            } else if (v instanceof Charset) {
                return (Charset) def;
            } else {
                return Charset.forName(v.toString());
            }
        }

        /**
         * Get objectName value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return ObjectName value.
         */
        @SuppressWarnings({"UseOfObsoleteCollectionType", "unchecked"})
        public static ObjectName getObjectName(Object v, ObjectName def) {
            if (v == null) {
                return def;
            } else if (v instanceof ObjectName) {
                return (ObjectName) v;
            } else if (v instanceof Object[] && ((Object[]) v).length == 2) {
                Object[] vs = (Object[]) v;
                String name = String.valueOf(vs[0]);
                java.util.Hashtable<String, String> ps =
                        (java.util.Hashtable<String, String>) (vs[1]);
                try {
                    return new ObjectName(name, ps);
                } catch (MalformedObjectNameException x) {
                    throw new IllegalArgumentException(
                            Arrays.deepToString(vs), x);
                }
            } else if (v instanceof List && ((List) v).size() == 2) {
                List l = (List) v;
                String name = String.valueOf(l.get(0));
                java.util.Hashtable<String, String> p =
                        (java.util.Hashtable<String, String>) (l.get(0));
                try {
                    return new ObjectName(name, p);
                } catch (MalformedObjectNameException x) {
                    throw new IllegalArgumentException(l.toString(), x);
                }
            } else {
                try {
                    return new ObjectName(v.toString());
                } catch (MalformedObjectNameException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            }
        }

        /**
         * Get UUID value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return UUID value.
         */
        public static UUID getUuid(Object v, UUID def) {
            if (v == null) {
                return def;
            } else if (v instanceof BigInteger
                    && ((BigInteger) v).bitCount() > 128) {
                ByteBuffer bb = ByteBuffer.allocate(16);
                bb.put(((BigInteger) v).toByteArray()).rewind();
                return new UUID(bb.getLong(), bb.getLong());
            } else if (v instanceof byte[]) {
                return UUID.nameUUIDFromBytes((byte[]) v);
            } else {
                return UUID.fromString(v.toString());
            }
        }

        /**
         * Get timeZone value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return TimeZone value.
         */
        public static TimeZone getTimeZone(Object v, TimeZone def) {
            if (v == null) {
                return def;
            } else if (v instanceof TimeZone) {
                return (TimeZone) v;
            } else {
                return TimeZone.getTimeZone(v.toString());
            }
        }

        /**
         * Get URI value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return URI value.
         */
        public static URI getUri(Object v, URI def) {
            if (v == null) {
                return def;
            } else if (v instanceof URI) {
                return (URI) v;
            } else if (v instanceof URL) {
                try {
                    return ((URL) v).toURI();
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof File) {
                return ((File) v).toURI();
            } else if (v instanceof Path) {
                return ((Path) v).toUri();
            } else if (v instanceof URLConnection) {
                try {
                    return ((URLConnection) v).getURL().toURI();
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else {
                try {
                    return new URI(v.toString());
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            }
        }

        /**
         * Get URL value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return URL value.
         */
        public static URL getUrl(Object v, URL def) {
            if (v == null) {
                return def;
            } else if (v instanceof URL) {
                return (URL) v;
            } else if (v instanceof URI) {
                try {
                    return ((URI) v).toURL();
                } catch (MalformedURLException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof File) {
                try {
                    return ((File) v).toURI().toURL();
                } catch (MalformedURLException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof Path) {
                try {
                    return ((Path) v).toUri().toURL();
                } catch (Exception x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof URLConnection) {
                return ((URLConnection) v).getURL();
            } else {
                try {
                    return new URL(v.toString());
                } catch (MalformedURLException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            }
        }

        /**
         * Get set value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Set value.
         */
        public static Set getSet(Object v, Set def) {
            if (v == null) {
                return def;
            } else if (v instanceof Set) {
                return (Set) v;
            } else if (v.getClass().isArray()) {
                HashSet<Object> hs = new HashSet<>();
                int n = Array.getLength(v);
                for (int i = 0; i < n; i++) {
                    hs.add(Array.get(v, i));
                }
                return hs;
            } else if (v instanceof Collection) {
                return new HashSet((Collection) v);
            } else if (v instanceof Map) {
                return ((Map) v).entrySet();
            } else {
                throw new IllegalArgumentException("Invalid set: " + v);
            }
        }

        /**
         * Get map value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Map value.
         */
        public static Map getMap(Object v, Map def) {
            if (v == null) {
                return def;
            } else if (v instanceof Map) {
                return (Map) v;
            } else {
                throw new IllegalArgumentException("Invalid map: " + v);
            }
        }

        /**
         * Get list value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return List value.
         */
        public static List getList(Object v, List def) {
            if (v == null) {
                return def;
            } else if (v instanceof List) {
                return (List) v;
            } else if (v.getClass().isArray()) {
                int n = Array.getLength(v);
                ArrayList<Object> l = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    l.add(Array.get(v, i));
                }
                return l;
            } else if (v instanceof Collection) {
                return new ArrayList((Collection) v);
            } else {
                throw new IllegalArgumentException("Invalid list: " + v);
            }
        }

        /**
         * Get file value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return File value.
         */
        public static File getFile(Object v, File def) {
            if (v == null) {
                return def;
            } else if (v instanceof File) {
                return (File) v;
            } else if (v instanceof Path) {
                return ((Path) v).toFile();
            } else if (v instanceof URI) {
                return new File((URI) v);
            } else if (v instanceof URL) {
                try {
                    return new File(((URL) v).toURI());
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof URLConnection) {
                try {
                    return new File(((URLConnection) v).getURL().toURI());
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else {
                return new File(v.toString());
            }
        }

        /**
         * Get path value.
         *
         * @param v Object value.
         * @param def Default value.
         * @return Path value.
         */
        public static Path getPath(Object v, Path def) {
            if (v == null) {
                return def;
            } else if (v instanceof Path) {
                return (Path) v;
            } else if (v instanceof File) {
                return ((File) v).toPath();
            } else if (v instanceof URI) {
                return Paths.get((URI) v);
            } else if (v instanceof String[]) {
                String[] vs = (String[]) v;
                return Paths.get(vs[0], Arrays.copyOfRange(vs, 1, vs.length));
            } else if (v instanceof List) {
                List<String> l = Collections.checkedList((List) v, String.class);
                return Paths.get(l.get(0), l.subList(1, l.size())
                        .toArray(new String[l.size() - 1]));
            } else if (v instanceof URL) {
                try {
                    return Paths.get(((URL) v).toURI());
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else if (v instanceof URLConnection) {
                try {
                    return Paths.get(((URLConnection) v).getURL().toURI());
                } catch (URISyntaxException x) {
                    throw new IllegalArgumentException(v.toString(), x);
                }
            } else {
                return Paths.get(v.toString());
            }
        }
    }
}
