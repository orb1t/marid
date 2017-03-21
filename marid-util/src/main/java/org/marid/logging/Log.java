/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

import org.intellij.lang.annotations.MagicConstant;
import org.marid.cache.MaridClassValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class Log {

    private static final ClassValue<Logger> LOGGER_CLASS_VALUE = new MaridClassValue<>(c -> () -> {
        final String name = c.getName();
        final int index = name.indexOf("$$");
        final String loggerName;
        if (index >= 0) {
            loggerName = name.substring(0, index);
        } else {
            loggerName = name;
        }
        return Logger.getLogger(loggerName);
    });

    public static void log(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nullable Throwable thrown,
                           @Nonnull Object... args) {
        final Logger logger = LOGGER_CLASS_VALUE.get(caller());
        final LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(null);
        record.setThrown(thrown);
        record.setParameters(args);
        logger.log(record);
    }

    public static void log(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nonnull Object... args) {
        final Logger logger = LOGGER_CLASS_VALUE.get(caller());
        final LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(null);
        record.setParameters(args);
        logger.log(record);
    }

    private static Class<?> caller() {
        final Class<?>[] classes = new SecurityPublicClassContext().getClassContext();
        return classes.length > 3 ? classes[3] : MethodHandles.lookup().lookupClass();
    }

    private static class SecurityPublicClassContext extends SecurityManager {
        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}
