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

package org.marid.lib.test

import org.junit.*;
import org.marid.db.util.IncMap

/**
 * Inc-map test.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
class IncMapTest {

    def testMap;
    def incMap;

    @Before
    void setUp() {
        testMap = [a: 10L, b: 20L, c: 30L];
        incMap = new IncMap(testMap, 1);
    }

    @Test
    void testGet() {
        assert incMap.a == 11;
        assert incMap.b == 21;
        assert incMap.c == 31;
    }

    @Test
    void testRemove() {
        assert incMap.remove("a") == 11;
        assert incMap.remove("b") == 21;
        assert incMap.remove("c") == 31;
    }

    @Test
    void testClear() {
        incMap.clear();
        assert incMap.isEmpty();
        assert testMap.isEmpty();
    }

    @Test
    void testSize() {
        assert testMap.size() == incMap.size();
    }

    @Test
    void testKeySet() {
        assert testMap.keySet() == incMap.keySet();
    }

    @Test
    void testValues() {
        assert testMap.values().collect{it + 1} == incMap.values().asList();
    }

    @Test
    void testEntrySet() {
        for (def i = 0; i < incMap.size(); i++) {
            def a1 = incMap.entrySet().toArray()[i];
            def a2 = incMap.entrySet().toArray()[i];
            assert a1 == a2;
            def b = testMap.entrySet().toArray()[i];
            assert b.value == a1.value - 1;
        }
        def e4 = new AbstractMap.SimpleEntry("d", 41L);
        incMap.entrySet().add(e4);
        assert incMap.d == 41;
        assert testMap.d == 40;
    }
}

