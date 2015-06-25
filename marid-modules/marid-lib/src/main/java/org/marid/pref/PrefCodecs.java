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

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.marid.dyn.Casting;
import org.marid.functions.SafeBiConsumer;
import org.marid.functions.SafeFunction;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.ServiceLoader.load;
import static org.marid.logging.LogSupport.Log.log;
import static org.marid.logging.LogSupport.WARNING;

/**
 * @author Dmitry Ovchinnikov
 */
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
        putReader(Path.class, stringReader(Paths::get));
        putReader(InetSocketAddress.class, stringReader(PrefCodecs::parseInetSocketAddress));
        putReader(InetAddress.class, stringReader(InetAddress::getByName));

        // Primitive writers
        putWriter(Integer.class, Preferences::putInt);
        putWriter(int.class, Preferences::putInt);
        putWriter(Long.class, Preferences::putLong);
        putWriter(long.class, Preferences::putLong);
        putWriter(Double.class, Preferences::putDouble);
        putWriter(double.class, Preferences::putDouble);
        putWriter(Short.class, (PrefWriter<Short>)Preferences::putInt);
        putWriter(short.class, (PrefWriter<Short>)Preferences::putInt);
        putWriter(Byte.class, (PrefWriter<Byte>)Preferences::putInt);
        putWriter(byte.class, (PrefWriter<Byte>)Preferences::putInt);
        putWriter(Float.class, Preferences::putFloat);
        putWriter(String.class, Preferences::put);
        putWriter(float.class, Preferences::putFloat);
        putWriter(Boolean.class, Preferences::putBoolean);
        putWriter(boolean.class, Preferences::putBoolean);
        putWriter(byte[].class, Preferences::putByteArray);
        putWriter(BigInteger.class, stringWriter(BigInteger::toString));
        putWriter(BigDecimal.class, stringWriter(BigDecimal::toString));
        putWriter(BitSet.class, (prefs, key, val) -> prefs.putByteArray(key, val.toByteArray()));
        putWriter(TimeZone.class, stringWriter(TimeZone::getID));
        putWriter(Currency.class, stringWriter(Currency::getCurrencyCode));
        putWriter(URL.class, stringWriter(URL::toString));
        putWriter(URI.class, stringWriter(URI::toString));
        putWriter(File.class, stringWriter(File::toString));
        putWriter(Path.class, stringWriter(Path::toString));
        putWriter(InetSocketAddress.class, stringWriter(InetSocketAddress::toString));
        putWriter(InetAddress.class, stringWriter(InetAddress::toString));

        // Custom readers and writers
        try {
            for (final PrefCodecs prefCodecs : load(PrefCodecs.class, Thread.currentThread().getContextClassLoader())) {
                READERS.putAll(prefCodecs.readers());
                WRITERS.putAll(prefCodecs.writers());
            }
        } catch (Exception x) {
            log(LOG, WARNING, "Unable to enumerate pref codecs", x);
        }
    }

    public abstract Map<Class<?>, PrefReader<?>> readers();

    public abstract Map<Class<?>, PrefWriter<?>> writers();

    private static <T> void putReader(Class<T> type, PrefReader<T> reader) {
        READERS.put(type, reader);
    }

    private static <T> void putWriter(Class<T> type, PrefWriter<T> writer) {
        WRITERS.put(type, writer);
    }

    protected static <T> PrefReader<T> stringReader(SafeFunction<String, T> function) {
        return (prefs, key) -> {
            final String v = prefs.get(key, null);
            return v == null ? null : function.applyUnsafe(v);
        };
    }

    protected static <T> PrefReader<T> splitReader(String separator, SafeFunction<String[], T> function) {
        return (pref, key) -> {
            final String v = pref.get(key, null);
            return v == null ? null : function.applyUnsafe(v.split(separator));
        };
    }

    protected static <T> PrefWriter<T> stringWriter(SafeFunction<T, String> function) {
        return (pref, key, value) -> pref.put(key, function.apply(value));
    }

    protected static <T> PrefWriter<T> formattedWriter(String format, SafeFunction<T, Object[]> function) {
        return (pref, key, value) -> pref.put(key, String.format(format, function.apply(value)));
    }

    protected static <T, K, V> PrefWriter<T> mapWriter(Class<K> keyType, Class<V> valueType, SafeBiConsumer<T, Map<K, V>> consumer) {
        return (pref, key, value) -> {
            final Map<K, V> map = new LinkedHashMap<>();
            consumer.accept(value, map);
            final String[] entries = map.entrySet().stream().map(e -> {
                final PrefWriter<K> keyWriter = getWriter(keyType);
                final PrefWriter<V> valueWriter = getWriter(valueType);
                final MapBasedPreferences preferences = new MapBasedPreferences();
                final String k, v;
                try {
                    keyWriter.save(preferences, "key", e.getKey());
                    valueWriter.save(preferences, "value", e.getValue());
                    k = URLEncoder.encode(preferences.getSpi("key"), "UTF-8");
                    v = URLEncoder.encode(preferences.getSpi("value"), "UTF-8");
                } catch (Exception x) {
                    throw new IllegalStateException(x);
                }
                return k + "=" + v;
            }).toArray(String[]::new);
            pref.put(key, String.join("|", entries));
        };
    }

    protected static <T, K, V> PrefReader<T> mapReader(Class<K> keyType, Class<V> valueType, SafeFunction<Map<K, V>, T> function) {
        return (prefs, key) -> {
            final String entriesText = prefs.get(key, null);
            if (entriesText == null) {
                return null;
            }
            final String[] entries = entriesText.split("[|]");
            final Map<K, V> map = new LinkedHashMap<>();
            final PrefReader<K> keyReader = getReader(keyType);
            final PrefReader<V> valueReader = getReader(valueType);
            final MapBasedPreferences preferences = new MapBasedPreferences();
            for (final String entry : entries) {
                final String[] kv = entry.split("=");
                preferences.putSpi("key", kv[0]);
                preferences.putSpi("value", kv[1]);
                map.put(keyReader.load(preferences, "key"), valueReader.load(preferences, "value"));
            }
            return function.apply(map);
        };
    }

    protected static <T> PrefReader<T> byteArrayReader(SafeFunction<byte[], T> function) {
        return (prefs, key) -> {
            final byte[] v = prefs.getByteArray(key, null);
            return v == null ? null : function.applyUnsafe(v);
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> PrefReader<T> getReader(Class<T> type) {
        final PrefReader<T> reader = (PrefReader<T>) READERS.get(type);
        if (reader != null) {
            return reader;
        } else if (type.isEnum()) {
            final Class<? extends Enum> enumType = type.asSubclass(Enum.class);
            return stringReader(s -> type.cast(Enum.valueOf(enumType, s)));
        } else {
            DefaultGroovyMethods.asType(1, Integer.class);
            return stringReader(s -> Casting.castTo(type, s));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> PrefWriter<T> getWriter(Class<T> type) {
        final PrefWriter<T> writer = (PrefWriter<T>) WRITERS.get(type);
        if (writer != null) {
            return writer;
        } else if (type.isEnum()) {
            return (pref, key, val) -> pref.put(key, ((Enum) val).name());
        } else {
            return (pref, key, val) -> pref.put(key, Casting.castTo(String.class, val));
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
