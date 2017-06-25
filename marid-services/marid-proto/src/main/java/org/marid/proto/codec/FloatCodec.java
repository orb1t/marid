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

package org.marid.proto.codec;

import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov
 */
public class FloatCodec implements Codec<Float> {

    @Override
    public Float decode(byte[] data) {
        return ByteBuffer.wrap(data).getFloat(0);
    }

    @Override
    public byte[] encode(Float data) {
        return ByteBuffer.allocate(4).putFloat(0, data).array();
    }
}
