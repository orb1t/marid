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

package org.marid.dpp;

import groovy.lang.Closure;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppFuncs {

    public static LinkedList<Closure> func(Iterable iterable) {
        final LinkedList<Closure> fs = new LinkedList<>();
        for (final Object o : iterable) {
            func(o, fs);
        }
        return fs;
    }

    public static void func(Object o, LinkedList<Closure> cs) {
        if (o instanceof Collection) {
            for (final Object co : (Collection) o) {
                func(co, cs);
            }
        } else if (o instanceof Closure) {
            cs.add((Closure) o);
        }
    }
}
