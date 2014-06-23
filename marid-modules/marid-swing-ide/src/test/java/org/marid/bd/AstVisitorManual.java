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

package org.marid.bd;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.io.StringWriter;

/**
 * @author Dmitry Ovchinnikov
 */
public class AstVisitorManual implements Opcodes {

    public static void main(String... args) throws Exception {
        final ClassNode node = new ClassNode("X", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        final Statement[] statements = {
                new ExpressionStatement(new DeclarationExpression(new VariableExpression("x"), Token.newSymbol(Types.EQUAL, -1, -1), new ConstantExpression(0)))
        };
        final Statement statement = new BlockStatement(statements, new VariableScope());
        final ConstructorNode constructorNode = new ConstructorNode(ACC_PUBLIC, statement);
        node.addConstructor(constructorNode);
        final StringWriter writer = new StringWriter();
        final AstNodeToScriptVisitor visitor = new AstNodeToScriptVisitor(writer);
        visitor.visitClass(node);
        System.out.println(writer);
    }
}
