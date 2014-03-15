/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.pref;

import org.marid.functions.UnsafeFunction;
import org.marid.methods.LogMethods;
import org.marid.util.Utils;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.ServiceLoader.load;
import static org.marid.dyn.TypeCaster.TYPE_CASTER;

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("unchecked")
public abstract class PrefCodecs {

    private static final Logger LOG = Logger.getLogger(PrefCodecs.class.getName());
    private static final Map<Class<?>, PrefReader<?>> READERS = new IdentityHashMap<>(256);
    private static final Map<Class<?>, PrefWriter<?>> WRITERS = new IdentityHashMap<>(256);

    static {
        // Primitive readers
        putReader(Integer.class, stringReader(Integer::decode));
        putReader(int.class, (prefs, key) -> prefs.getInt(key, 0));
        putReader(Long.class, stringReader(Long::decode));
        putReader(long.class, (prefs, key) -> prefs.getLong(key, 0L));
        putReader(Short.class, stringReader(Short::decode));
        putReader(short.class, (prefs, key) -> (short) prefs.getInt(key, 0));
        putReader(Byte.class, stringReader(Byte::decode));
        putReader(byte.class, (prefs, key) -> (byte) prefs.getInt(key, 0));
        putReader(Double.class, stringReader(Double::valueOf));
        putReader(double.class, (prefs, key) -> prefs.getDouble(key, 0.0));
        putReader(Float.class, stringReader(Float::valueOf));
        putReader(float.class, (prefs, key) -> prefs.getFloat(key, 0.0f));
        putReader(String.class, (prefs, key) -> prefs.get(key, null));
        putReader(Boolean.class, stringReader(Boolean::valueOf));
        putReader(boolean.class, (prefs, key) -> prefs.getBoolean(key, false));
        putReader(byte[].class, (prefs, key) -> prefs.getByteArray(key, null));
        putReader(BigInteger.class, stringReader(BigInteger::new));
        putReader(BigDecimal.class, stringReader(BigDecimal::new));
        putReader(BitSet.class, byteArrayReader(BitSet::valueOf));
        putReader(TimeZone.class, stringReader(TimeZone::getTimeZone));
        putReader(Currency.class, stringReader(Currency::getInstance));
        putReader(URL.class, stringReader(URL::new));
        putReader(URI.class, stringReader(URI::new));
        putReader(File.class, stringReader(File::new));
        putReader(InetSocketAddress.class, stringReader(PrefCodecs::parseInetSocketAddress));
        putReader(InetAddress.class, stringReader(InetAddress::getByName));

        // Primitive writers
        putWriter(Integer.class, Preferences::putInt);
        putWriter(int.class, Preferences::putInt);
        putWriter(Long.class, Preferences::putLong);
        putWriter(long.class, Preferences::putLong);
        putWriter(Double.class, Preferences::putDouble);
        putWriter(double.class, Preferences::putDouble);
        putWriter(Short.class, Preferences::putInt);
        putWriter(short.class, Preferences::putInt);
        putWriter(Byte.class, Preferences::putInt);
        putWriter(byte.class, Preferences::putInt);
        putWriter(Float.class, Preferences::putFloat);
        putWriter(String.class, Preferences::put);
        putWriter(float.class, Preferences::putFloat);
        putWriter(Boolean.class, Preferences::putBoolean);
        putWriter(boolean.class, Preferences::putBoolean);
        putWriter(byte[].class, Preferences::putByteArray);
        putWriter(BigInteger.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(BigDecimal.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(BitSet.class, (prefs, key, val) -> prefs.putByteArray(key, val.toByteArray()));
        putWriter(TimeZone.class, (prefs, key, val) -> prefs.put(key, val.getID()));
        putWriter(Currency.class, (prefs, key, val) -> prefs.put(key, val.getCurrencyCode()));
        putWriter(URL.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(URI.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(File.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(InetSocketAddress.class, (prefs, key, val) -> prefs.put(key, val.toString()));
        putWriter(InetAddress.class, (prefs, key, val) -> prefs.put(key, val.toString()));

        // Custom readers and writers
        try {
            for (final PrefCodecs prefCodecs : load(PrefCodecs.class, Utils.getClassLoader(PrefCodecs.class))) {
                READERS.putAll(prefCodecs.readers());
                WRITERS.putAll(prefCodecs.writers());
            }
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to enumerate pref codecs", x);
        }
        final Map<Class<?>, PrefReader<Object>> arrayReaders = new IdentityHashMap<>();
        final Map<Class<?>, PrefWriter<Object>> arrayWriters = new IdentityHashMap<>();
        for (final Map.Entry<Class<?>, PrefReader<?>> e : READERS.entrySet()) {
            final Class<?> arrayType = Array.newInstance(e.getKey(), 0).getClass();
            arrayReaders.put(arrayType, (prefs, key) -> {
                if (prefs.nodeExists(key)) {
                    final Preferences node = prefs.node(key);
                    final String[] keys = node.keys();
                    final int n = keys.length;
                    final Object array = Array.newInstance(e.getKey(), n);
                    for (int i = 0; i < n; i++) {
                        Array.set(array, i, e.getValue().load(node, keys[i]));
                    }
                    return array;
                } else {
                    return null;
                }
            });
        }
        for (final Map.Entry<Class<?>, PrefWriter<?>> e : WRITERS.entrySet()) {
            final Class<?> arrayType = Array.newInstance(e.getKey(), 0).getClass();
            arrayWriters.put(arrayType, (prefs, key, val) -> {
                final Preferences node = prefs.node(key);
                for (final String k : node.keys()) {
                    node.remove(k);
                }
                final int n = Array.getLength(val);
                for (int i = 0; i < n; i++) {
                    ((PrefWriter<Object>) e.getValue()).save(node, Integer.toString(i), Array.get(val, i));
                }
            });
        }
        READERS.putAll(arrayReaders);
        WRITERS.putAll(arrayWriters);
    }

    public abstract Map<Class<?>, PrefReader<?>> readers();

    public abstract Map<Class<?>, PrefWriter<?>> writers();

    private static <T> void putReader(Class<T> type, PrefReader<T> reader) {
        READERS.put(type, reader);
    }

    private static <T> void putWriter(Class<T> type, PrefWriter<T> writer) {
        WRITERS.put(type, writer);
    }

    protected static <T> PrefReader<T> stringReader(UnsafeFunction<String, T> function) {
        return (prefs, key) -> {
            final String v = prefs.get(key, null);
            return v == null ? null : function.applyUnsafe(v);
        };
    }

    protected static <T> PrefReader<T> splitReader(String separator, UnsafeFunction<String[], T> function) {
        return (pref, key) -> {
            final String v = pref.get(key, null);
            return v == null ? null : function.applyUnsafe(v.split(separator));
        };
    }

    protected static <T> PrefReader<T> byteArrayReader(UnsafeFunction<byte[], T> function) {
        return (prefs, key) -> {
            final byte[] v = prefs.getByteArray(key, null);
            return v == null ? null : function.applyUnsafe(v);
        };
    }

    public static <T> PrefReader<T> getReader(Class<T> type) {
        final PrefReader<T> reader = (PrefReader<T>) READERS.get(type);
        if (reader != null) {
            return reader;
        } else if (type.isEnum()) {
            return stringReader(s -> (T) Enum.valueOf((Class<Enum>) type, s));
        } else {
            return stringReader(s -> TYPE_CASTER.cast(type, s));
        }
    }

    public static <T> PrefWriter<T> getWriter(Class<T> type) {
        final PrefWriter<T> writer = (PrefWriter<T>) WRITERS.get(type);
        if (writer != null) {
            return writer;
        } else if (type.isEnum()) {
            return (pref, key, val) -> pref.put(key, val.toString());
        } else {
            return (pref, key, val) -> pref.put(key, TYPE_CASTER.cast(String.class, val));
        }
    }

    public static InetSocketAddress parseInetSocketAddress(String value) throws Exception {
        final URI uri = new URI("proto://" + value);
        return InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
    }

    public static class ReaderMapBuilder {

        private final Map<Class<?>, PrefReader<?>> readerMap = new IdentityHashMap<>();

        public <T> ReaderMapBuilder add(Class<T> type, PrefReader<T> reader) {
            readerMap.put(type, reader);
            return this;
        }

        public Map<Class<?>, PrefReader<?>> build() {
            return readerMap;
        }
    }

    public static class WriterMapBuilder {

        private final Map<Class<?>, PrefWriter<?>> writerMap = new IdentityHashMap<>();

        public <T> WriterMapBuilder add(Class<T> type, PrefWriter<T> writer) {
            writerMap.put(type, writer);
            return this;
        }

        public Map<Class<?>, PrefWriter<?>> build() {
            return writerMap;
        }
    }
}
