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

package org.marid.bd.constant;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.marid.bd.components.NamedBlockComponentEditor;
import org.marid.groovy.GroovyRuntime;
import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Vector;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlockEditor extends NamedBlockComponentEditor<ConstantBlock> implements LogSupport {

    protected final JComboBox<String> valueCombo;

    public ConstantBlockEditor(Window window, ConstantBlock constantBlock) {
        super(window, constantBlock);
        tabPane("Common").addLine("Value", valueCombo = new JComboBox<>(getExpressions()));
        valueCombo.setEditable(true);
        afterInit();
    }

    @Override
    protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
        final Object value = GroovyRuntime.SHELL.evaluate(valueCombo.getSelectedItem().toString(), "expt.groovy");
        block.setValue((ConstantExpression) value);
    }

    private static Vector<String> getExpressions() {
        final Vector<String> vector = new Vector<>();
        final String cexpr = ConstantExpression.class.getCanonicalName();
        for (final Field field : ConstantExpression.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == ConstantExpression.class) {
                vector.add(String.format("%s.%s", cexpr, field.getName()));
            }
        }
        return vector;
    }
}
