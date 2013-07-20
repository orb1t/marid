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

package org.marid.data;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataTest {

    private MapValue mapValue;

    @Before
    public void init() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2L);
        map.put("c", new int[] {0, -1, 100});
        Map<String, Object> childMap = new LinkedHashMap<>();
        childMap.put("1", -33);
        map.put("cm", childMap);
        mapValue = new MapValue(map);
    }

    @Test
    public void testMaps() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(MapValue.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter sw = new StringWriter();
        m.marshal(mapValue, sw);
        System.out.println(sw);
        Unmarshaller u = ctx.createUnmarshaller();
        MapValue clone = (MapValue) u.unmarshal(new StringReader(sw.toString()));
        assertEquals(mapValue, clone);
    }
}
