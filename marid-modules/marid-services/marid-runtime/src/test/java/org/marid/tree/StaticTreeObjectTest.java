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

package org.marid.tree;

import org.junit.Test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class StaticTreeObjectTest {

    private final StaticTreeObject root = new StaticTreeObject(null, "root", emptyMap());

    public StaticTreeObjectTest() {
        final StaticTreeObject e1 = new StaticTreeObject(root, "e1", singletonMap("a", 1));
        final StaticTreeObject e2 = new StaticTreeObject(root, "e2", singletonMap("b", 2.0));
        root.children.put("e1", e1);
        root.children.put("e2", e2);
        final StaticTreeObject e11 = new StaticTreeObject(e1, "e11", singletonMap("c", 3.0f));
        final StaticTreeObject e21 = new StaticTreeObject(e2, "e21", singletonMap("d", 4L));
        e1.children.put("e11", e11);
        e2.children.put("e21", e21);
    }

    @Test
    public void testName() {
        assertEquals("root", root.name());
    }

    @Test
    public void testNested() {
        assertEquals("e1", root.object("e1").name());
        assertEquals("e2", root.object("e2").name());
        assertEquals("e11", root.object("e1").object("e11").name());
        assertEquals("e21", root.object("e2").object("e21").name());
    }

    @Test
    public void testTreeNavigation() {
        for (final StaticTreeObject child : root.children.values()) {
            assertEquals(child, root.object(child.name()));
            assertEquals(child, root.object(".", child.name()));
            assertEquals(child, root.object(".", child.name(), "..", child.name()));
        }
    }
}
