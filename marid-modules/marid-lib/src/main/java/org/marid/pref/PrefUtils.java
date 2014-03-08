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
import org.marid.methods.LogMethods;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class PrefUtils {

    private static final Logger LOG = Logger.getLogger(PrefUtils.class.getName());

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
        try {
            return PrefCodecs.getReader(type).load(preferences, key, def);
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to load {0}.{1} of {2}", x, preferences, key, type);
            return def;
        }
    }

    @SuppressWarnings("unchecked")
    public static  <T> T getPref(Preferences preferences, String key, T def) {
        try {
            return PrefCodecs.getReader((Class<T>)def.getClass()).load(preferences, key, def);
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to load {0}.{1} of {2}", x, preferences, key, def == null ? null : def.getClass());
            return def;
        }
    }

    public static <T> void putPref(Preferences preferences, Class<T> type, String key, T value) {
        try {
            PrefCodecs.getWriter(type).save(preferences, key, value);
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to save {0}.{1} value {2}", preferences, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static void putPref(Preferences preferences, String key, Object value) {
        try {
            PrefCodecs.getWriter((Class) value.getClass()).save(preferences, key, value);
        } catch (Exception x) {
            LogMethods.warning(LOG, "Unable to save {0}.{1} value {2}", preferences, key, value);
        }
    }

    public static String[] parseStrings(String value) {
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        } else {
            final String[] parts = trimmed.split("\\s");
            for (int i = 0; i < parts.length; i++) {
                final String part = parts[i];
                if (part.indexOf(0xA0) >= 0) {
                    final StringBuilder builder = new StringBuilder(part);
                    for (int p = 0; p < builder.length(); p++) {
                        if (builder.charAt(p) == 0xA0) {
                            if (p + 1 < builder.length() && builder.charAt(p + 1) == 0xA0) {
                                builder.replace(p, p + 2, "\u00A0");
                            } else {
                                builder.setCharAt(p, ' ');
                            }
                        }
                    }
                    parts[i] = builder.toString();
                }
            }
            return parts;
        }
    }

    public static String makeStrings(String[] value) {
        if (value.length == 0) {
            return "";
        } else {
            final String[] parts = new String[value.length];
            for (int i = 0; i < parts.length; i++) {
                final String part = value[i].trim();
                if (part.indexOf(' ') >= 0) {
                    parts[i] = part.replace("\u00A0", "\u00A0\u00A0").replace(' ', '\u00A0');
                } else {
                    parts[i] = part;
                }
            }
            return String.join(" ", parts);
        }
    }
}
