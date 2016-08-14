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

package org.marid.proto.modbus;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov
 */
public class ModbusIntArrayCodec implements ModbusCodec<int[]> {

    private final ModbusIntCodec codec;

    public ModbusIntArrayCodec(ModbusTwoRegisterOrder order) {
        this.codec = new ModbusIntCodec(order);
    }

    @Override
    public int[] decode(byte[] data) {
        return range(0, data.length / 4).map(i -> codec.decode(copyOfRange(data, 4 * i, 4 * i + 4))).toArray();
    }

    @Override
    public byte[] encode(int[] data) {
        final byte[] res = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(codec.encode(data[i]), 0, res, 4 * i, 4);
        }
        return res;
    }
}
