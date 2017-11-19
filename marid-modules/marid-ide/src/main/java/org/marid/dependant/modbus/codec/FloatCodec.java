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
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class FloatCodec extends ModbusCodec<Float> {

  @NotNull
  @Override
  public String getName() {
    return "Float";
  }

  @NotNull
  @Override
  public byte[] encode(@NotNull Float value) {
    return ByteBuffer.allocate(4).putInt(0, Float.floatToIntBits(value)).array();
  }

  @NotNull
  @Override
  public Float decode(@NotNull byte[] value) {
    return Float.intBitsToFloat(ByteBuffer.wrap(value).getInt(0));
  }

  @Override
  public int getSize() {
    return 4;
  }
}
