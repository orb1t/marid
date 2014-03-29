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

package org.marid.swing.dnd;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class DndSourceTest {

    @Test
    public void testGenerics() {
        final DndSource<?> dndSource = new DndSource<TestDndObject>() {
        };
        final DataFlavor[] dataFlavors = dndSource.getSourceDataFlavors();
        Assert.assertEquals(1, dataFlavors.length);
        Assert.assertEquals(new DataFlavor(TestDndObject.class, null), dataFlavors[0]);
    }

    class TestDndObject implements DndObject {

        @Override
        public Object getObject() {
            return null;
        }
    }
}
