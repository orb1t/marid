package org.marid.jfx.action;

import java.io.File;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public interface Dialogs {

    static File overrideExt(File original, String ext) {
        return original == null
                ? null
                : original.getName().endsWith(ext)
                ? original
                : new File(original.getParentFile(), original.getName() + ext);
    }
}
