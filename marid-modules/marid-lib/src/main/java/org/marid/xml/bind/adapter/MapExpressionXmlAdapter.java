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

package org.marid.xml.bind.adapter;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class MapExpressionXmlAdapter extends XmlAdapter<String, MapExpression> {

    @Override
    public MapExpression unmarshal(String v) throws Exception {
        final AstBuilder builder = new AstBuilder();
        final List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, v);
        final BlockStatement blockStatement = (BlockStatement) nodes.get(0);
        final ExpressionStatement expressionStatement = (ExpressionStatement) blockStatement.getStatements().get(0);
        return (MapExpression) expressionStatement.getExpression();
    }

    @Override
    public String marshal(MapExpression v) throws Exception {
        final StringWriter writer = new StringWriter();
        final AstNodeToScriptVisitor astNodeToScriptVisitor = new AstNodeToScriptVisitor(writer);
        astNodeToScriptVisitor.visitMapExpression(v);
        return writer.toString();
    }
}
