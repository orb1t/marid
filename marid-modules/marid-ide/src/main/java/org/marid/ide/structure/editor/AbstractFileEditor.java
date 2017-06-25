/*
 *
 */

package org.marid.ide.structure.editor;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
