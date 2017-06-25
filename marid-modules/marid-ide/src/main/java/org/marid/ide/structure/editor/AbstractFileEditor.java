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
