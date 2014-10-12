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

package org.marid.bd.blocks.expressions;

import org.codehaus.groovy.ast.expr.Expression;
import org.marid.bd.ConfigurableBlock;
import org.marid.bd.IoBlock;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.components.DefaultBlockComponentEditor;

import javax.swing.*;
import java.awt.*;
import java.util.EventListener;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock
public class NamedExpressionBlock extends IoBlock implements ConfigurableBlock {

    protected Expression expression;
    protected String key;

    public NamedExpressionBlock() {
        super("Named Expression Block", "n.expr", "e -> n.e", Color.BLUE, Expression.class, NamedExpression.class);
    }

    @Override
    public void reset() {
        key = "value";
        expression = null;
    }

    @Override
    protected void set(Object value) {
        expression = (Expression) value;
    }

    @Override
    protected NamedExpression get() {
        return new NamedExpression(expression, key);
    }

    public void setName(String newKey) {
        fire(NamedExpressionBlockListener.class, () -> key, n -> key = n, newKey, NamedExpressionBlockListener::keyChanged);
    }

    @Override
    public String getLabel() {
        return key == null ? "value" : key;
    }

    @Override
    public Window createWindow(Window parent) {
        final JTextField nameField = new JTextField(name);
        return new DefaultBlockComponentEditor<>(parent, this,
                ed -> ed.tabPane("Common").addLine("Name", nameField),
                (ed, a, e) -> setName(nameField.getText()));
    }

    public static class NamedExpression {

        public final Expression expression;
        public final String name;

        public NamedExpression(Expression expression, String name) {
            this.expression = expression;
            this.name = name;
        }
    }

    public interface NamedExpressionBlockListener extends EventListener {

        void keyChanged(String key);
    }
}
