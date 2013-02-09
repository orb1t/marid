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

package org.marid.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggingGroovyMethods {

    /**
     * Logs an info record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void info(Logger logger, String msg, Object... args) {
        logger.logp(Level.INFO, null, null, msg, args);
    }

    /**
     * Logs a fine record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void fine(Logger logger, String msg, Object... args) {
        logger.logp(Level.FINE, null, null, msg, args);
    }

    /**
     * Logs a finer record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void finer(Logger logger, String msg, Object... args) {
        logger.logp(Level.FINER, null, null, msg, args);
    }

    /**
     * Logs a finest record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void finest(Logger logger, String msg, Object... args) {
        logger.logp(Level.FINEST, null, null, msg, args);
    }

    /**
     * Logs a warning record.
     * @param logger Current logger.
     * @param msg Message.
     * @param error An error.
     * @param args Arguments.
     */
    public static void warning(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.WARNING, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        logger.log(r);
    }

    /**
     * Logs a warning record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void warning(Logger logger, String msg, Object... args) {
        logger.logp(Level.WARNING, null, null, msg, args);
    }

    /**
     * Logs a severe record.
     * @param logger Current logger.
     * @param msg Message.
     * @param error An error.
     * @param args Arguments.
     */
    public static void severe(Logger logger, String msg, Throwable error, Object... args) {
        LogRecord r = new LogRecord(Level.SEVERE, msg);
        r.setParameters(args);
        r.setThrown(error);
        r.setSourceClassName(null);
        logger.log(r);
    }

    /**
     * Logs a severe record.
     * @param logger Current logger.
     * @param msg Message.
     * @param args Arguments.
     */
    public static void severe(Logger logger, String msg, Object... args) {
        logger.logp(Level.SEVERE, null, null, msg, args);
    }
}
