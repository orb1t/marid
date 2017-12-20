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
import java.util.concurrent.Callable;
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
}
