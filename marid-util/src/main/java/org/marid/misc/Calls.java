/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.misc;

import org.jetbrains.annotations.NotNull;
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

  static <T> T call(@NotNull Callable<T> func, @NotNull Function<Exception, RuntimeException> xfunc) {
    try {
      return func.call();
    } catch (RuntimeException x) {
      throw x;
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    } catch (Exception x) {
      throw xfunc.apply(x);
    }
  }

  static <T> T call(@NotNull Callable<T> func) {
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

  static void callWithTime(@NotNull Runnable task, Consumer<Duration> durationConsumer) {
    final long startTime = System.nanoTime();
    task.run();
    durationConsumer.accept(Duration.ofNanos(System.nanoTime() - startTime));
  }

  static <T> T callWithTime(@NotNull Callable<T> task, BiConsumer<Duration, Exception> timeConsumer) {
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
