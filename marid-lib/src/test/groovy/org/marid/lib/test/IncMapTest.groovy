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

import javax.naming.CompositeName
import org.marid.db.util.IncMap

/**
 * Inc-map test.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
class IncMapTest extends GroovyTestCase {

    static def k1 = new CompositeName("a");
    static def k2 = new CompositeName("b");
    static def k3 = new CompositeName("c");

    def testMap;
    def incMap;

    @Override
    void setUp() {
        testMap = [(k1): 10L, (k2): 20L, (k3): 30L];
        incMap = new IncMap(testMap, 1);
    }

    void testGet() {
        assert incMap[k1] == 11;
        assert incMap[k2] == 21;
        assert incMap[k3] == 31;
    }

    void testRemove() {
        assert incMap.remove(k1) == 11;
        assert incMap.remove(k2) == 21;
        assert incMap.remove(k3) == 31;
    }

    void testClear() {
        incMap.clear();
        assert incMap.isEmpty();
        assert testMap.isEmpty();
    }

    void testSize() {
        assert testMap.size() == incMap.size();
    }

    void testKeySet() {
        assert testMap.keySet() == incMap.keySet();
    }

    void testValues() {
        assert testMap.values().collect{it + 1} == incMap.values().asList();
    }

    void testEntrySet() {
        for (def i = 0; i < incMap.size(); i++) {
            def a1 = incMap.entrySet().toArray()[i];
            def a2 = incMap.entrySet().toArray()[i];
            assert a1 == a2;
            def b = testMap.entrySet().toArray()[i];
            assert b.value == a1.value - 1;
        }
    }
}

