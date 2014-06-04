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

package org.marid.groovy;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.io.StringWriter;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class AstTest {

    @Test
    public void testExpression1() throws Exception {
        final BinaryExpression binaryExpression = new BinaryExpression(
                new ConstantExpression(1),
                new Token(Types.PLUS, "+", -1, -1),
                new ConstantExpression(2));
        final StringWriter writer = new StringWriter();
        final AstNodeToScriptVisitor scriptVisitor = new AstNodeToScriptVisitor(writer);
        scriptVisitor.visitBinaryExpression(binaryExpression);
        System.out.println(writer);
    }
}
