/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.bd;

import org.junit.Assert;
import org.junit.Test;
import org.marid.logging.LogSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockTest implements LogSupport {

    @Test
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new TestBlockA(10));
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final TestBlockA block = (TestBlockA) ois.readObject();
            info("Block: {0}", block);
            Assert.assertEquals(10, block.q);
        }
    }
}
