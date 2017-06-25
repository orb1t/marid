package org.marid.dependant.modbus.codec;

import org.jetbrains.annotations.NotNull;
import org.marid.misc.Casts;
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public abstract class ModbusCodec<T> {

    @NotNull
    @Nonnull
    public abstract String getName();

    @NotNull
    @Nonnull
    public abstract byte[] encode(@Nonnull T value);

    @NotNull
    @Nonnull
    public  abstract T decode(@Nonnull byte[] value);

    public abstract int getSize();

    public Class<T> getType() {
        final ResolvableType type = ResolvableType.forClass(ModbusCodec.class, getClass());
        final ResolvableType arg = type.getGeneric(0);
        return Casts.cast(arg.getRawClass());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && ((ModbusCodec) obj).getName().equals(getName());
    }
}
