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
public interface PrefSupport {

    default String[] prefNodes() {
        return new String[] {getClass().getSimpleName()};
    }

    default Preferences preferences() {
        return PrefUtils.preferences(getClass(), prefNodes());
    }

    default <T> T getPref(Class<T> type, String key, T def) {
        return PrefCodecs.getReader(type).load(preferences(), key, def);
    }

    @SuppressWarnings("unchecked")
    default <T> T getPref(String key, T def) {
        return PrefCodecs.getReader((Class<T>)def.getClass()).load(preferences(), key, def);
    }

    default <T> void putPref(Class<T> type, String key, T value) {
        PrefCodecs.getWriter(type).save(preferences(), key, value);
    }

    @SuppressWarnings("unchecked")
    default void putPref(String key, Object value) {
        PrefCodecs.getWriter((Class) value.getClass()).save(preferences(), key, value);
    }
}
