package org.marid.manual;

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
