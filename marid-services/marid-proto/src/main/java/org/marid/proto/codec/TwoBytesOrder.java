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

/**
 * @author Dmitry Ovchinnikov
 */
public enum TwoBytesOrder implements Codec<byte[]> {

    ABCD(0, 1, 2, 3),
    BADC(1, 0, 3, 2),
    DCBA(3, 2, 1, 0),
    CDAB(2, 3, 0, 1);

    private final int[] indices;

    TwoBytesOrder(int... indices) {
        this.indices = indices;
    }

    @Override
    public byte[] decode(byte[] data) {
        final byte[] res = new byte[4];
        for (int i = 0; i < indices.length; i++) {
            res[i] = data[indices[i]];
        }
        return res;
    }

    @Override
    public byte[] encode(byte[] data) {
        final byte[] res = new byte[4];
        for (int i = 0; i < indices.length; i++) {
            res[indices[i]] = data[i];
        }
        return res;
    }
}
