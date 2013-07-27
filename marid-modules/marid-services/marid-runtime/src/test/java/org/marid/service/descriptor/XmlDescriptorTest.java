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

package org.marid.service.descriptor;

import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
public class XmlDescriptorTest {

    private final Map<String, Object> bindings = new HashMap<>();

    public XmlDescriptorTest() {
        bindings.put("k1", 1);
        bindings.put("k2", 2.3);
        bindings.put("k3", "[]");
        bindings.put("k4", "$k3");
        bindings.put("ks", "$kr");
        bindings.put("kr", "$ks");
        Map<String, Object> cm = new HashMap<>();
        cm.put("k1", 100.2);
        bindings.put("k5", cm);
    }

    @Test
    public void testBaseDescriptor() throws Exception {
        URL url = getClass().getResource("base-descriptor.xml");
        XmlDescriptor descriptor = Descriptors.loadDescriptor(XmlDescriptor.class, bindings, url);
        assertEquals("1", descriptor.getId());
    }
}
