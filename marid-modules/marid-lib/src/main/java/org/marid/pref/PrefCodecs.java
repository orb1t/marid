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

import org.marid.methods.LogMethods;
import org.marid.util.Utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
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
public abstract class PrefCodecs {

    private static final Logger LOG = Logger.getLogger(PrefCodecs.class.getName());
    private static final Map<Class<?>, PrefReader<?>> READERS = new IdentityHashMap<>(256);
    private static final Map<Class<?>, PrefWriter<?>> WRITERS = new IdentityHashMap<>(256);

    static {
        // Primitive readers
        putReader(Integer.class, Preferences::getInt);
        putReader(int.class, Preferences::getInt);
        putReader(Long.class, Preferences::getLong);
        putReader(long.class, Preferences::getLong);
        putReader(Short.class, (prefs, key, def) -> (short) prefs.getInt(key, def));
        putReader(short.class, (prefs, key, def) -> (short) prefs.getInt(key, def));
        putReader(Byte.class, (prefs, key, def) -> (byte) prefs.getInt(key, def));
        putReader(byte.class, (prefs, key, def) -> (byte) prefs.getInt(key, def));
        putReader(Double.class, Preferences::getDouble);
        putReader(double.class, Preferences::getDouble);
        putReader(Float.class, Preferences::getFloat);
        putReader(float.class, Preferences::getFloat);
        putReader(String.class, Preferences::get);
        putReader(Boolean.class, Preferences::getBoolean);
        putReader(boolean.class, Preferences::getBoolean);
        putReader(byte[].class, Preferences::getByteArray);
        putReader(BigInteger.class, (prefs, key, def) -> new BigInteger(prefs.get(key, def.toString())));
        putReader(BigDecimal.class, (prefs, key, def) -> new BigDecimal(prefs.get(key, def.toString())));
        putReader(BitSet.class, (prefs, key, def) -> BitSet.valueOf(prefs.getByteArray(key, def.toByteArray())));
        putReader(TimeZone.class, (prefs, key, def) -> TimeZone.getTimeZone(prefs.get(key, def.getID())));
        putReader(Currency.class, (prefs, key, def) -> Currency.getInstance(prefs.get(key, def.getCurrencyCode())));
        putReader(URL.class, (prefs, key, def) -> new URL(prefs.get(key, def.toString())));
        putReader(URI.class, (prefs, key, def) -> new URI(prefs.get(key, def.toString())));
        putReader(File.class, (prefs, key, def) -> new File(prefs.get(key, def.toString())));

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

        // Custom readers and writers
        try {
            for (final PrefCodecs prefCodecs : load(PrefCodecs.class, Utils.getClassLoader(PrefCodecs.class))) {
                READERS.putAll(prefCodecs.readers());
                WRITERS.putAll(prefCodecs.writers());
            }
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to enumerate pref codecs", x);
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

    @SuppressWarnings("unchecked")
    public static <T> PrefReader<T> getReader(Class<T> type) {
        final PrefReader<T> reader = (PrefReader<T>) READERS.get(type);
        if (reader != null) {
            return reader;
        } else if (type.isEnum()) {
            return (pref, key, def) -> (T) Enum.valueOf((Class<Enum>)type, def.toString());
        } else {
            return (pref, key, def) -> {
                final String val = pref.get(key, null);
                return val != null ? TYPE_CASTER.cast(type, val) : def;
            };
        }
    }

    @SuppressWarnings("unchecked")
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
}
