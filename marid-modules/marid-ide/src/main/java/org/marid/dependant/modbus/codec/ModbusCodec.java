/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.modbus.codec;

import org.jetbrains.annotations.NotNull;
import org.marid.misc.Casts;
import org.springframework.core.ResolvableType;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public abstract class ModbusCodec<T> {

  @NotNull
  public abstract String getName();

  @NotNull
  public abstract byte[] encode(@NotNull T value);

  @NotNull
  public abstract T decode(@NotNull byte[] value);

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
