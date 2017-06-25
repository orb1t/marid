package org.marid.misc;

import javax.annotation.Nullable;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Casts {

    @SuppressWarnings("unchecked")
    static <T> T cast(@Nullable Object object) {
        return (T) object;
    }
}
