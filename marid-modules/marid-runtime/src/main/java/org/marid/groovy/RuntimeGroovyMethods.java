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

package org.marid.groovy;

import groovy.lang.Closure;
import org.marid.Marid;
import org.marid.logging.LogSupport;
import org.marid.methods.LogMethods;

import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RuntimeGroovyMethods {

    public static void warning(Object object, String message, Throwable thrown, Object... args) {
        LogMethods.warning(logger(object), message, thrown, args);
    }

    public static void warning(Object object, String message, Object... args) {
        LogMethods.warning(logger(object), message, args);
    }

    public static void info(Object object, String message, Object... args) {
        LogMethods.info(logger(object), message, args);
    }

    public static void severe(Object object, String message, Throwable thrown, Object... args) {
        LogMethods.severe(logger(object), message, thrown, args);
    }

    public static void severe(Object object, String message, Object... args) {
        LogMethods.severe(logger(object), message, args);
    }

    public static void fine(Object object, String message, Object...args) {
        LogMethods.fine(logger(object), message, args);
    }

    public static void finer(Object object, String message, Object...args) {
        LogMethods.finer(logger(object), message, args);
    }

    public static void finest(Object object, String message, Object...args) {
        LogMethods.finest(logger(object), message, args);
    }

    public static void config(Object object, String message, Object... args) {
        LogMethods.config(logger(object), message, args);
    }

    private static Logger logger(Object object) {
        if (object instanceof LogSupport) {
            return ((LogSupport) object).logger();
        } else if (object instanceof Closure) {
            return logger(((Closure) object).getOwner());
        } else {
            return Marid.LOGGER;
        }
    }
}
