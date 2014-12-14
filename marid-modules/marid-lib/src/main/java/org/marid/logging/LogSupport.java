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

import org.marid.methods.LogMethods;
import org.marid.util.MaridClassValue;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.marid.util.MaridClassValue.getCaller;

/**
 * @author Dmitry Ovchinnikov
 */
public interface LogSupport {

    ClassValue<Logger> LOGGERS = new MaridClassValue<>(c -> Logger.getLogger(c.getName()));

    default Logger logger() {
        return LOGGERS.get(getClass());
    }

    default void info(String message, Object... args) {
        LogMethods.info(logger(), message, args);
    }

    default void warning(String message, Object... args) {
        LogMethods.warning(logger(), message, args);
    }

    default void warning(String message, Throwable throwable, Object... args) {
        LogMethods.warning(logger(), message, throwable, args);
    }

    default void severe(String message, Object... args) {
        LogMethods.severe(logger(), message, args);
    }

    default void severe(String message, Throwable throwable, Object... args) {
        LogMethods.severe(logger(), message, throwable, args);
    }

    default void config(String message, Object... args) {
        LogMethods.config(logger(), message, args);
    }

    default void fine(String message, Object... args) {
        LogMethods.fine(logger(), message, args);
    }

    default void finer(String message, Object... args) {
        LogMethods.finer(logger(), message, args);
    }

    default void finest(String message, Object... args) {
        LogMethods.finest(logger(), message, args);
    }

    default void log(Level level, String message, Object... args) {
        LogMethods.log(logger(), level, message, null, args);
    }

    default void log(Level level, String message, Throwable throwable, Object... args) {
        LogMethods.log(logger(), level, message, throwable, args);
    }

    static class Log {

        public static void info(String message, Object... args) {
            LogMethods.info(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void warning(String message, Object... args) {
            LogMethods.warning(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void warning(String message, Throwable throwable, Object... args) {
            LogMethods.warning(LOGGERS.get(getCaller(3)), message, throwable, args);
        }

        public static void severe(String message, Object... args) {
            LogMethods.warning(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void severe(String message, Throwable throwable, Object... args) {
            LogMethods.severe(LOGGERS.get(getCaller(3)), message, throwable, args);
        }

        public static void config(String message, Object... args) {
            LogMethods.config(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void fine(String message, Object... args) {
            LogMethods.fine(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void finer(String message, Object... args) {
            LogMethods.finer(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void finest(String message, Object... args) {
            LogMethods.finest(LOGGERS.get(getCaller(3)), message, args);
        }

        public static void log(Level level, String message, Object... args) {
            LogMethods.log(LOGGERS.get(getCaller(3)), level, message, null, args);
        }

        public static void log(Level level, String message, Throwable throwable, Object... args) {
            LogMethods.log(LOGGERS.get(getCaller(3)), level, message, throwable, args);
        }
    }
}
