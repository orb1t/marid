package org.marid.runtime.beans;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanProperties {

    @Nonnull
    public final Type[] types;

    @Nonnull
    public final MethodHandle[] setters;

    public BeanProperties(@Nonnull Type[] types, @Nonnull MethodHandle[] setters) {
        this.types = types;
        this.setters = setters;
    }
}
