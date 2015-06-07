/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.xml.bind;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;
import org.marid.xml.bind.adapter.MapExpressionXmlAdapter;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class MapExpressionXmlAdapterTest {

    @Test
    public void testMapExpressionCodec() throws Exception {
        final MapExpressionXmlAdapter adapter = new MapExpressionXmlAdapter();
        final MapExpression mapExpression = adapter.unmarshal("[x: 1, z : 2 * 10 + Math.sin(1)]");
        final String code = adapter.marshal(mapExpression);
        final GroovyShell shell = new GroovyShell();
        final Map map = (Map) shell.evaluate(code);
        Assert.assertEquals(1, map.get("x"));
        Assert.assertEquals(2 * 10 + Math.sin(1), map.get("z"));
    }
}
