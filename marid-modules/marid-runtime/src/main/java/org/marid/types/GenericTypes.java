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

package org.marid.types;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.String.format;
import static org.marid.groovy.GroovyRuntime.SHELL;

/**
 * @author Dmitry Ovchinnikov.
 */
public class GenericTypes {

    private static final Map<String, Type> TYPE_MAP = new WeakHashMap<>();

    public static Type getType(String type) {
        synchronized (TYPE_MAP) {
            return TYPE_MAP.computeIfAbsent(type, t -> {
                final String code = format("%s m() {null}\n getClass().getMethod('m').genericReturnType", t);
                return (Type) SHELL.evaluate(code);
            });
        }
    }
}
