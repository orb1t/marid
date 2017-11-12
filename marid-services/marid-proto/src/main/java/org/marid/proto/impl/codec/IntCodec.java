/*-
 * #%L
 * marid-proto
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

package org.marid.proto.impl.codec;

import org.marid.runtime.annotation.MaridBean;

import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov
 */
@MaridBean(name = "4-bytes integer codec")
public class IntCodec implements Codec<Integer> {

  @Override
  public Integer decode(byte[] data) {
    return ByteBuffer.wrap(data).getInt(0);
  }

  @Override
  public byte[] encode(Integer data) {
    return ByteBuffer.allocate(4).putInt(0, data).array();
  }
}
