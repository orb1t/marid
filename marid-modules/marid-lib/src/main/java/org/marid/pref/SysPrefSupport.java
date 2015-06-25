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
 * @author Dmitry Ovchinnikov.
 */
public interface SysPrefSupport {

    Preferences SYSPREFS = PrefUtils.preferences("system");

    default void logSysPrefError(boolean load, Class<?> type, String key, String[] nodes, Throwable error) {
        final String fmt = load ? "Unable to load system {0} of {1}" : "Unable to save system {0} of {1}";
        final String k = nodes == null || nodes.length == 0 ? key : String.join(".", nodes) + "." + key;
        if (this instanceof LogSupport) {
            ((LogSupport) this).log(Level.WARNING, fmt, error.getCause(), k, type);
        } else {
            LogSupport.Log.log(Logger.getLogger(getClass().getName()), Level.WARNING, fmt, error.getCause(), k, type);
        }
    }

    default <T> T getSysPref(Class<T> type, String key, T def, String... nodes) {
        try {
            return PrefUtils.getPref(SYSPREFS, type, key, def, nodes);
        } catch (Exception x) {
            logSysPrefError(true, type, key, nodes, x);
            return def;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T getSysPref(String key, T def, String... nodes) {
        try {
            return PrefUtils.getPref(SYSPREFS, key, def, nodes);
        } catch (Exception x) {
            logSysPrefError(true, def == null ? null : def.getClass(), key, nodes, x);
            return def;
        }
    }

    default <T> void putSysPref(Class<T> type, String key, T value, String... nodes) {
        try {
            PrefUtils.putPref(SYSPREFS, type, key, value, nodes);
        } catch (Exception x) {
            logSysPrefError(false, type, key, nodes, x);
        }
    }

    @SuppressWarnings("unchecked")
    default void putSysPref(String key, Object value, String... nodes) {
        try {
            PrefUtils.putPref(SYSPREFS, key, value, nodes);
        } catch (Exception x) {
            logSysPrefError(false, value == null ? null : value.getClass(), key, nodes, x);
        }
    }
}
