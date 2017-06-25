package org.marid.logging;

import org.intellij.lang.annotations.MagicConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Logs {

    @Nonnull
    Logger logger();

    default void log(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                     @Nonnull String message,
                     @Nullable Throwable thrown,
                     @Nonnull Object... args) {
        final Logger logger = logger();
        final LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(null);
        record.setThrown(thrown);
        record.setParameters(args);
        logger.log(record);
    }

    default void log(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                     @Nonnull String message,
                     @Nonnull Object... args) {
        final Logger logger = logger();
        final LogRecord record = new LogRecord(level, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(null);
        record.setParameters(args);
        logger.log(record);
    }
}
