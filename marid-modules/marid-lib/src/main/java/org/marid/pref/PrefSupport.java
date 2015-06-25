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

import org.marid.logging.LogSupport;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public interface PrefSupport {

    default String[] prefNodes() {
        return new String[] {getClass().getSimpleName()};
    }

    default Preferences preferences() {
        return PrefUtils.preferences(getClass(), prefNodes());
    }

    default void logPrefError(boolean load, Class<?> type, String key, String[] nodes, Throwable error) {
        final String fmt = load ? "Unable to load {0} of {1}" : "Unable to save {0} of {1}";
        final String k = nodes == null || nodes.length == 0 ? key : String.join(".", nodes) + "." + key;
        if (this instanceof LogSupport) {
            ((LogSupport) this).log(Level.WARNING, fmt, error.getCause(), k, type);
        } else {
            LogSupport.Log.log(Logger.getLogger(getClass().getName()), Level.WARNING, fmt, error.getCause(), k, type);
        }
    }

    default <T> T getPref(Class<T> type, String key, T def, String... nodes) {
        try {
            return PrefUtils.getPref(preferences(), type, key, def, nodes);
        } catch (Exception x) {
            logPrefError(true, type, key, nodes, x);
            return def;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T getPref(String key, T def, String... nodes) {
        try {
            return PrefUtils.getPref(preferences(), key, def, nodes);
        } catch (Exception x) {
            logPrefError(true, def == null ? null : def.getClass(), key, nodes, x);
            return def;
        }
    }

    default <T> void putPref(Class<T> type, String key, T value, String... nodes) {
        try {
            PrefUtils.putPref(preferences(), type, key, value, nodes);
        } catch (Exception x) {
            logPrefError(false, type, key, nodes, x);
        }
    }

    default void putPref(String key, Object value, String... nodes) {
        try {
            PrefUtils.putPref(preferences(), key, value, nodes);
        } catch (Exception x) {
            logPrefError(false, value == null ? null : value.getClass(), key, nodes, x);
        }
    }
}
