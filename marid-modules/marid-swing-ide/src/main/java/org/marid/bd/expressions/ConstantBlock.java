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

package org.marid.bd.expressions;

import images.Images;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.marid.bd.NamedBlock;
import org.marid.bd.NamedBlockListener;
import org.marid.bd.components.NamedBlockComponentEditor;
import org.marid.bd.components.StandardBlockComponent;
import org.marid.groovy.GroovyRuntime;
import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.ConstructorProperties;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlock extends NamedBlock {

    protected String value = "null";
    protected final Out<ConstantExpression> output = new Out<>("", ConstantExpression.class, this::constantExpression);

    public ConstantBlock() {
        name = "Constant block";
    }

    @ConstructorProperties({"value"})
    public ConstantBlock(String value) {
        this();
        this.value = value;
    }

    @Override
    public StandardBlockComponent<ConstantBlock> createComponent() {
        final JLabel titleLabel = new JLabel(name);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        final JLabel label = new JLabel(value);
        return new StandardBlockComponent<>(this, c -> {
            final JPanel panel = new JPanel(new BorderLayout(0, 5));
            panel.setOpaque(false);
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(label);
            addEventListener(c, new ConstantBlockListener() {
                @Override
                public void changedValue(String oldValue, String newValue) {
                    label.setText(newValue);
                    update();
                }

                @Override
                public void nameChanged(String oldName, String newName) {
                    titleLabel.setText(newName);
                    update();
                }

                void update() {
                    c.validate();
                    c.setSize(c.getPreferredSize());
                    c.getSchemaEditor().repaint();
                }
            });
            c.add(panel);
        });
    }

    @Override
    public ImageIcon getVisualRepresentation() {
        return Images.getIconFromText("const", 32, 32, Color.BLUE, Color.WHITE);
    }

    @Override
    public ConstantBlockEditor createWindow(Window parent) {
        return new ConstantBlockEditor(parent);
    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(output);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        fire(ConstantBlockListener.class, () -> value, v -> value = v, newValue, ConstantBlockListener::changedValue);
    }

    public ConstantExpression constantExpression() {
        final Object o = GroovyRuntime.SHELL.evaluate(value, "expression.groovy");
        return new ConstantExpression(o);
    }

    public class ConstantBlockEditor extends NamedBlockComponentEditor<ConstantBlock> implements LogSupport {

        protected final JComboBox<String> valueCombo;

        public ConstantBlockEditor(Window window) {
            super(window, ConstantBlock.this);
            tabPane("Common").addLine("Value", valueCombo = new JComboBox<>(getExpressions()));
            valueCombo.setEditable(true);
            valueCombo.setSelectedItem(block.getValue());
            afterInit();
        }

        @Override
        protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
            block.setValue(valueCombo.getSelectedItem().toString());
        }

        private Vector<String> getExpressions() {
            final Vector<String> vector = new Vector<>();
            for (final Field field : ConstantExpression.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ConstantExpression.class) {
                    try {
                        final ConstantExpression constantExpression = (ConstantExpression) field.get(null);
                        final Object v = constantExpression.getValue();
                        if (v instanceof String) {
                            vector.add('"' + String.valueOf(v) + '"');
                        } else if (v instanceof Class<?>) {
                            vector.add(((Class<?>) v).getSimpleName());
                        } else {
                            vector.add(String.valueOf(v));
                        }
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException(x);
                    }
                }
            }
            return vector;
        }
    }

    public interface ConstantBlockListener extends NamedBlockListener {

        void changedValue(String oldValue, String newValue);
    }
}