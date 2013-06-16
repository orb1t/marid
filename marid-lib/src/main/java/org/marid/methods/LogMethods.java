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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogMethods {

    public static void info(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.INFO, msg);
        r.setParameters(args);
        r.setSourceMethodName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void fine(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINE, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void finer(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINER, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void finest(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.FINEST, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void warning(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.WARNING, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void warning(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.WARNING, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void severe(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }

    public static void severe(Logger logger, String msg, Object... args) {
        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setParameters(args);
        r.setSourceClassName(null);
        r.setLoggerName(logger.getName());
        logger.log(r);
    }
}
