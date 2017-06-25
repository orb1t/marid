/*
 *
 */

package org.marid.manual;

/*-
 * #%L
 * marid-util
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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.ManualTests;
import org.openjdk.jol.info.GraphLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({ManualTests.class})
public class MapLayoutTest {

    @Test
    public void hashMapTest() {
        final Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        GraphLayout graphLayout = GraphLayout.parseInstance(map);
        for (final Map.Entry<String, Object> e : map.entrySet()) {
            graphLayout = graphLayout.subtract(GraphLayout.parseInstance(e.getKey()));
            graphLayout = graphLayout.subtract(GraphLayout.parseInstance(e.getValue()));
        }
        System.out.println(graphLayout.toFootprint());
    }
}
