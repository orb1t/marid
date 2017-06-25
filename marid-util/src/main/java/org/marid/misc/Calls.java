package org.marid.misc;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Calls {

    static <T> T call(@Nonnull Callable<T> func, @Nonnull Function<Exception, RuntimeException> xfunc) {
        try {
            return func.call();
        } catch (Exception x) {
            throw xfunc.apply(x);
        }
    }

    static <T> T call(@Nonnull Callable<T> func) {
        return call(func, IllegalStateException::new);
    }

    static <T, R> Function<T, R> func(Function<T, Callable<R>> function) {
        return arg -> {
            final Callable<R> callable = function.apply(arg);
            try {
                return callable.call();
            } catch (RuntimeException x) {
                throw x;
            } catch (IOException x) {
                throw new UncheckedIOException(x);
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        };
    }

    static void callWithTime(@Nonnull Runnable task, Consumer<Duration> durationConsumer) {
        final long startTime = System.nanoTime();
        task.run();
        durationConsumer.accept(Duration.ofNanos(System.nanoTime() - startTime));
    }

    static <T> T callWithTime(@Nonnull Callable<T> task, BiConsumer<Duration, Exception> timeConsumer) {
        final long startTime = System.nanoTime();
        Exception exception = null;
        T result = null;
        try {
            result = task.call();
        } catch (Exception x) {
            exception = x;
        }
        timeConsumer.accept(Duration.ofNanos(System.nanoTime() - startTime), exception);
        return result;
    }
}
