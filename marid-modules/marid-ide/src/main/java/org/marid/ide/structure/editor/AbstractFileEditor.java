/*-
 * #%L
 * marid-ide
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

package org.marid.ide.structure.editor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractFileEditor<T> implements FileEditor {

  private final PathMatcher pathMatcher;

  public AbstractFileEditor(@NotNull PathMatcher... pathMatchers) {
    pathMatcher = of(pathMatchers).reduce((m1, m2) -> p -> m1.matches(p) || m2.matches(p)).orElse(p -> true);
  }

  @Nullable
  protected abstract T editorContext(@NotNull Path path);

  protected abstract void edit(@NotNull Path path, @NotNull T context);

  @Nullable
  @Override
  public Runnable getEditAction(@NotNull Path path) {
    if (!pathMatcher.matches(path)) {
      return null;
    } else {
      final T context = editorContext(path);
      if (context != null) {
        return () -> edit(path, context);
      } else {
        return null;
      }
    }
  }
}
