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

import org.marid.function.SafeFunction;
import org.marid.util.StringUtils;
import org.marid.util.Utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class PrefCodecs {

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
        putReader(InetAddress.class, stringReader(InetAddress::getByName));
        putReader(String[].class, stringReader(s -> of(s.split(",")).map(StringUtils::urlDecode).toArray(String[]::new)));
        putReader(Level.class, stringReader(Level::parse));

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
        putWriter(InetAddress.class, stringWriter(InetAddress::toString));
        putWriter(String[].class, stringWriter(s -> of(s).map(StringUtils::urlEncode).collect(joining(","))));
        putWriter(Level.class, stringWriter(Level::getName));

        // Custom readers and writers
        try {
            for (final PrefCodecs prefCodecs : load(PrefCodecs.class, Thread.currentThread().getContextClassLoader())) {
                READERS.putAll(prefCodecs.readers());
                WRITERS.putAll(prefCodecs.writers());
            }
        } catch (Exception x) {
            x.printStackTrace(System.err); // log infrastructure initialization order workaround
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

    protected static <T> PrefWriter<T> stringWriter(SafeFunction<T, String> function) {
        return (pref, key, value) -> pref.put(key, function.apply(value));
    }

    protected static <T> PrefReader<T> byteArrayReader(SafeFunction<byte[], T> function) {
        return (prefs, key) -> {
            final byte[] v = prefs.getByteArray(key, null);
            return v == null ? null : function.applyUnsafe(v);
        };
    }

    public static <T> PrefReader<T> getReader(Class<T> type) {
        final PrefReader<T> reader = Utils.cast(READERS.get(type));
        if (reader != null) {
            return reader;
        } else if (type.isEnum()) {
            final Class<Enum<?>> enumType = Utils.cast(type);
            return stringReader(s -> {
                final Object object = Enum.valueOf(Utils.cast(enumType), s);
                return Utils.cast(object);
            });
        } else {
            throw new IllegalArgumentException("Preference reader for " + type + " is not found");
        }
    }

    public static <T> PrefWriter<T> getWriter(Class<T> type) {
        final PrefWriter<T> writer = Utils.cast(WRITERS.get(type));
        if (writer != null) {
            return writer;
        } else if (type.isEnum()) {
            return (pref, key, val) -> pref.put(key, ((Enum) val).name());
        } else {
            throw new IllegalArgumentException("Preference writer for " + type + " is not found");
        }
    }
}
