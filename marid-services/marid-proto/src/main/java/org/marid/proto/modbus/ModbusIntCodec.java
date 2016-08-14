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

import com.google.common.primitives.Ints;

/**
 * @author Dmitry Ovchinnikov
 */
public class ModbusIntCodec implements ModbusCodec<Integer> {

    private final ModbusTwoRegisterOrder order;

    public ModbusIntCodec(ModbusTwoRegisterOrder order) {
        this.order = order;
    }

    @Override
    public Integer decode(byte[] data) {
        return Ints.fromByteArray(order.decode(data));
    }

    @Override
    public byte[] encode(Integer data) {
        return order.encode(Ints.toByteArray(data));
    }
}
