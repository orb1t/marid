/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.structure.editor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractFileEditor<T> implements FileEditor {


    private final PathMatcher pathMatcher;

    public AbstractFileEditor(@Nonnull PathMatcher... pathMatchers) {
        pathMatcher = of(pathMatchers).reduce((m1, m2) -> p -> m1.matches(p) || m2.matches(p)).orElse(p -> true);
    }

    @Nullable
    protected abstract T editorContext(@Nonnull Path path);

    protected abstract void edit(@Nonnull Path path, @Nonnull T context);

    @Nullable
    @Override
    public Runnable getEditAction(@Nonnull Path path) {
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
