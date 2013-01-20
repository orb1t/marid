/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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
package org.marid;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import junit.framework.TestCase;

/**
 * Miscellaneous test.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MiscTest extends TestCase {

    public void test1() {
        Random r = new Random();
        BitSet bs = new BitSet(8);
        for (int i = 0; i < 8; i++) {
            bs.set(i, true);
        }
        System.out.println(bs);
        System.out.println(Arrays.toString(bs.toByteArray()));
        byte b = -1;
        System.out.println(b & 0b10000000);
    }
}
