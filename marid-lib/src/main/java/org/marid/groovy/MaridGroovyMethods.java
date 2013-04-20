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

package org.marid.groovy;

import org.marid.l10n.Localized;
import org.marid.pref.PrefObject;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridGroovyMethods {

    public static void info(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.INFO, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void fine(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINE, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void finer(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINER, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void finest(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINEST, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void warning(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.WARNING, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void warning(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.WARNING, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void severe(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static void severe(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        logger.log(r);
    }

    public static String ls(String text, Object... args) {
        return Localized.S.l(text, args);
    }

    public static String lm(String text, Object... args) {
        return Localized.M.l(text, args);
    }

    public static Preferences getPreferences(PrefObject prefObject) {
        return Preferences.userNodeForPackage(prefObject.getClass()).node(prefObject.getPrefNode());
    }

    public static Preferences getPreferences(Class<?> rootClass) {
        return Preferences.userNodeForPackage(rootClass).node(rootClass.getSimpleName());
    }
}
