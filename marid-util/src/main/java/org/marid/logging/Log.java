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
        log(LOGGER_CLASS_VALUE.get(caller(3)), level, message, thrown, args);
    }

    public static void log(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nonnull Object... args) {
        log(LOGGER_CLASS_VALUE.get(caller(3)), level, message, args);
    }

    public static void log(int depth,
                           @Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nullable Throwable thrown,
                           @Nonnull Object... args) {
        log(LOGGER_CLASS_VALUE.get(caller(depth)), level, message, thrown, args);
    }

    public static void log(@Nonnull Logger logger,
                           @Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nullable Throwable thrown,
                           @Nonnull Object... args) {
        final LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(null);
        record.setThrown(thrown);
        record.setParameters(args);
        logger.log(record);
    }

    public static void log(@Nonnull Logger logger,
                           @Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nonnull Object... args) {
        log(logger, level, message, null, args);
    }

    public static boolean isLoggable(Level level) {
        final Logger logger = LOGGER_CLASS_VALUE.get(caller(3));
        return logger.isLoggable(level);
    }

    private static Class<?> caller(int depth) {
        final Class<?>[] classes = new SecurityPublicClassContext().getClassContext();
        return classes.length > depth ? classes[depth] : MethodHandles.lookup().lookupClass();
    }

    private static class SecurityPublicClassContext extends SecurityManager {
        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}
