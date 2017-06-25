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

package org.marid.proto.codec;

import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class DoubleCodec implements Codec<Double> {

    private final int size;

    public DoubleCodec(int size) {
        this.size = size;
        if (size != 4 && size != 8) {
            throw new IllegalArgumentException("Incorrect size: " + size);
        }
    }

    @Override
    public Double decode(byte[] data) {
        switch (data.length) {
            case 4:
                return (double) ByteBuffer.wrap(data).getFloat(0);
            case 8:
                return ByteBuffer.wrap(data).getDouble(0);
            default:
                throw new IllegalArgumentException(Base64.getEncoder().encodeToString(data));
        }
    }

    @Override
    public byte[] encode(Double data) {
        switch (size) {
            case 4:
                return ByteBuffer.allocate(4).putFloat(0, data.floatValue()).array();
            case 8:
                return ByteBuffer.allocate(8).putDouble(0, data).array();
            default:
                throw new IllegalArgumentException("Size: " + size);
        }
    }
}
