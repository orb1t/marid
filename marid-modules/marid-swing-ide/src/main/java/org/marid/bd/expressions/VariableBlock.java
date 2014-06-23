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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.marid.bd.IoBlock;

import java.awt.*;
import java.beans.ConstructorProperties;

/**
 * @author Dmitry Ovchinnikov
 */
public class VariableBlock extends IoBlock<ClassNode, VariableExpression> {

    protected ClassNode type;
    protected String varName = "x";

    public VariableBlock() {
        super("Variable expression", "*", ">", Images.getIconFromText("var", 32, 32, Color.BLUE, Color.WHITE));
    }

    @ConstructorProperties({"varName"})
    public VariableBlock(String varName) {
        this();
        this.varName = varName;
    }

    @Override
    public void set(ClassNode value) {
        type = value;
    }

    @Override
    public void reset() {
        type = null;
    }

    @Override
    public VariableExpression get() {
        return new VariableExpression(varName, type);
    }

    public String getVarName() {
        return varName;
    }
}
