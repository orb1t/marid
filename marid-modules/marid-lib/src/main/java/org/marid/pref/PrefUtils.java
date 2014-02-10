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

import org.marid.Versioning;

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class PrefUtils {

    public static Preferences preferences(Class<?> klass, String... nodes) {
        final String version = Versioning.getImplementationVersion(klass);
        Preferences prefs = Preferences.userRoot().node("marid").node(version);
        for (String n : nodes) {
            prefs = prefs.node(n);
        }
        return prefs;
    }

    public static Preferences preferences(String... nodes) {
        return preferences(Versioning.class, nodes);
    }

    public static <T> T getPref(Preferences preferences, Class<T> type, String key, T def) {
        return PrefCodecs.getReader(type).load(preferences, key, def);
    }

    @SuppressWarnings("unchecked")
    public static  <T> T getPref(Preferences preferences, String key, T def) {
        return PrefCodecs.getReader((Class<T>)def.getClass()).load(preferences, key, def);
    }

    public static <T> void putPref(Preferences preferences, Class<T> type, String key, T value) {
        PrefCodecs.getWriter(type).save(preferences, key, value);
    }

    @SuppressWarnings("unchecked")
    public static void putPref(Preferences preferences, String key, Object value) {
        PrefCodecs.getWriter((Class) value.getClass()).save(preferences, key, value);
    }
}
