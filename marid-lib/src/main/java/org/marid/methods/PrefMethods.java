/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.methods;

import org.marid.Scripting;

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class PrefMethods {

    public static Preferences preferences(Class<?> klass, String... nodes) {
        Package pkg = klass.getPackage();
        String version = pkg.getSpecificationVersion();
        if (version == null) {
            version = "DEV";
        }
        Preferences prefs = Preferences.userRoot().node("marid").node(version);
        for (String n : nodes) {
            prefs = prefs.node(n);
        }
        return prefs;
    }

    public static Preferences preferences(String... nodes) {
        return preferences(Scripting.class, nodes);
    }
}