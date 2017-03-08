/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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
