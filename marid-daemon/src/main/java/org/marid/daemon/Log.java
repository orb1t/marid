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

package org.marid.daemon;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class Log {

    public static void info(Logger log, String message, Object... args) {
        log(log, Level.INFO, message, args);
    }

    public static void config(Logger log, String message, Object... args) {
        log(log, Level.CONFIG, message, args);
    }

    public static void fine(Logger log, String message, Object... args) {
        log(log, Level.FINE, message, args);
    }

    public static void finer(Logger log, String message, Object... args) {
        log(log, Level.FINER, message, args);
    }

    public static void finest(Logger log, String message, Object... args) {
        log(log, Level.FINEST, message, args);
    }

    public static void warning(Logger log, String message, Throwable thrown, Object... args) {
        log(log, Level.WARNING, message, thrown, args);
    }

    public static void warning(Logger log, String message, Object... args) {
        log(log, Level.WARNING, message, args);
    }

    public static void severe(Logger log, String message, Throwable thrown, Object... args) {
        log(log, Level.SEVERE, message, thrown, args);
    }

    public static void severe(Logger log, String message, Object... args) {
        log(log, Level.SEVERE, message, args);
    }

    public static void log(Logger log, Level level, String message, Object... args) {
        final LogRecord logRecord = new LogRecord(level, message);
        logRecord.setLoggerName(log.getName());
        logRecord.setParameters(args);
        logRecord.setSourceClassName(null);
        log.log(logRecord);
    }

    public static void log(Logger log, Level level, String message, Throwable thrown, Object... args) {
        final LogRecord logRecord = new LogRecord(level, message);
        logRecord.setLoggerName(log.getName());
        logRecord.setParameters(args);
        logRecord.setThrown(thrown);
        logRecord.setSourceClassName(null);
        log.log(logRecord);
    }
}
