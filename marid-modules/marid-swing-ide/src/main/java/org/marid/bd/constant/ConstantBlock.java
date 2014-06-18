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

import images.Images;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.marid.bd.NamedBlock;

import javax.swing.*;
import java.awt.*;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConstantBlock extends NamedBlock {

    protected String value = ConstantExpression.class.getCanonicalName() + ".EMPTY_EXPRESSION";
    protected final Out<ConstantExpression> output = new Out<>(ConstantExpression.class, "out", () -> value);

    public ConstantBlock() {
        name = "Constant block";
    }

    @ConstructorProperties({"value"})
    public ConstantBlock(String value) {
        this();
        this.value = value;
    }

    @Override
    public ConstantBlockComponent createComponent() {
        return new ConstantBlockComponent(this);
    }

    @Override
    public ImageIcon getVisualRepresentation() {
        return Images.getIcon("block/constant.png");
    }

    @Override
    public ConstantBlockEditor createWindow(Window parent) {
        return new ConstantBlockEditor(parent, this);
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

    public void setValue(String value) {
        final String old = this.value;
        this.value = value;
        fireEvent(ConstantBlockListener.class, l -> l.changedValue(old, value));
    }
}
