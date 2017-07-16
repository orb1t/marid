package org.marid.runtime.beans;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanConstructor {

    @Nonnull
    public final Type type;

    @Nonnull
    public final Type[] args;

    @Nonnull
    public final MethodHandle handle;

    public BeanConstructor(@Nonnull Type type, @Nonnull Type[] args, @Nonnull MethodHandle handle) {
        this.type = type;
        this.args = args;
        this.handle = handle;
    }
}
