package org.marid.ide.structure.editor;

import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.action.SpecialAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public interface FileEditor {

    @Nullable
    Runnable getEditAction(@Nonnull Path path);

    @Nonnull
    String getName();

    @Nonnull
    String getIcon();

    @Nonnull
    String getGroup();

    @Nullable
    default SpecialAction getSpecialAction() {
        return null;
    }

    default String icon(@PropertyKey(resourceBundle = "fonts.meta") String icon) {
        return icon;
    }
}
