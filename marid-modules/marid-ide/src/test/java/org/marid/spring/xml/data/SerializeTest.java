/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring.xml.data;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.marid.spring.xml.BeanProp;
import org.marid.test.NormalTests;

import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
@RunWith(JUnitParamsRunner.class)
public class SerializeTest {

    private static BeanProp property(String name, String ref, String value) {
        final BeanProp property = new BeanProp();
        property.name.set(name);
        return property;
    }

    public Object[][] propParams() {
        return new Object[][] {
                {property("x", "y", null)},
                {property("x", null, null)},
                {property(null, null, "abcdef")}
        };
    }

    @Parameters(method = "propParams")
    @Test
    public void testProperties(BeanProp property) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream(100);
        try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(property);
        }
        final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        try (final ObjectInputStream ois = new ObjectInputStream(is)) {
            final BeanProp cloned = (BeanProp) ois.readObject();
            assertEquals(property.name.get(), cloned.name.get());
        }
    }
}
