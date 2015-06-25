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
package org.marid.logging;

import org.marid.util.MaridClassValue;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.marid.util.MaridClassValue.getCaller;

/**
 * @author Dmitry Ovchinnikov
 */
public interface LogSupport {

    ClassValue<Logger> LOGGERS = new MaridClassValue<>(c -> Logger.getLogger(c.getName()));

    Level INFO = Level.INFO;
    Level WARNING = Level.WARNING;
    Level SEVERE = Level.SEVERE;
    Level CONFIG = Level.CONFIG;
    Level FINE = Level.FINE;
    Level FINER = Level.FINER;
    Level FINEST = Level.FINEST;
    Level ALL = Level.ALL;
    Level OFF = Level.OFF;

    default Logger logger() {
        return LOGGERS.get(getClass());
    }

    default void log(Level level, String message, Object... args) {
        Log.log(logger(), level, message, null, args);
    }

    default void log(Level level, String message, Throwable throwable, Object... args) {
        Log.log(logger(), level, message, throwable, args);
    }

    class Log {

        public static void log(Level level, String message, Object... args) {
            log(LOGGERS.get(getCaller(3)), level, message, null, args);
        }

        public static void log(Level level, String message, Throwable throwable, Object... args) {
            log(LOGGERS.get(getCaller(3)), level, message, throwable, args);
        }

        public static void log(Logger logger, Level level, String msg, Throwable throwable, Object... args) {
            if (logger.isLoggable(level)) {
                final LogRecord r = new LogRecord(level, msg);
                r.setParameters(args);
                r.setSourceClassName(null);
                r.setLoggerName(logger.getName());
                r.setThrown(throwable);
                logger.log(r);
            }
        }
    }
}
