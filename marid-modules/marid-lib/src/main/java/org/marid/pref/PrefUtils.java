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

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class PrefUtils {

    private static String classVersion(Class<?> klass) {
        final Package pkg = klass.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        } else {
            return System.getProperties().getProperty("implementation.version", "DEV");
        }
    }

    public static Preferences preferences(Class<?> klass, String... nodes) {
        final String version = classVersion(klass);
        Preferences prefs = Preferences.userRoot().node("marid").node(version).node(klass.getCanonicalName());
        for (String n : nodes) {
            prefs = prefs.node(n);
        }
        return prefs;
    }

    public static Preferences preferences(String... nodes) {
        return preferences(PrefUtils.class, nodes);
    }

    public static <T> T getPref(Preferences preferences, Class<T> type, String key, T def, String... nodes) {
        Preferences p = preferences;
        for (final String node : nodes) {
            p = p.node(node);
        }
        try {
            return PrefCodecs.getReader(type).load(p, key, def);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPref(Preferences preferences, String key, T def, String... nodes) {
        Preferences p = preferences;
        for (final String node : nodes) {
            p = p.node(node);
        }
        try {
            return PrefCodecs.getReader((Class<T>)def.getClass()).load(p, key, def);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> void putPref(Preferences preferences, Class<T> type, String key, T value, String... nodes) {
        Preferences p = preferences;
        for (final String node : nodes) {
            p = p.node(node);
        }
        try {
            PrefCodecs.getWriter(type).save(p, key, value);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @SuppressWarnings("unchecked")
    public static void putPref(Preferences preferences, String key, Object value, String... nodes) {
        Preferences p = preferences;
        for (final String node : nodes) {
            p = p.node(node);
        }
        try {
            PrefCodecs.getWriter((Class) value.getClass()).save(p, key, value);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
