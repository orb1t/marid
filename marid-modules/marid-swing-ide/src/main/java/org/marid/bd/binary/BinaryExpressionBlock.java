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

package org.marid.bd.binary;

import images.Images;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.marid.bd.Block;
import org.marid.bd.BlockComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class BinaryExpressionBlock extends Block {

    protected Expression leftExpression;
    protected Expression rightExpression;
    protected TokenType tokenType;
    protected final In<Expression> leftInput = new In<>("left", Expression.class, e -> leftExpression = e);
    protected final In<Expression> rightInput = new In<>("right", Expression.class, e -> rightExpression = e);
    protected final Out<Expression> output = new Out<>("result", Expression.class,
            () -> new BinaryExpression(leftExpression, tokenType.token, rightExpression));

    public BinaryExpressionBlock() {
        this(TokenType.PLUS);
    }

    @ConstructorProperties({"tokenType"})
    public BinaryExpressionBlock(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public BlockComponent createComponent() {
        return new BinaryExpressionBlockComponent(this);
    }

    @Override
    public BinaryExpressionEditor createWindow(Window parent) {
        return new BinaryExpressionEditor(parent, this);
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(leftInput, rightInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(output);
    }

    @Override
    public String getName() {
        return "Binary Expression";
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public static enum TokenType {

        PLUS(Token.newPlaceholder(Types.PLUS), "tokens/plus.png"),
        MINUS(Token.newPlaceholder(Types.MINUS), "tokens/minus.png");

        public final Token token;
        public final ImageIcon icon;

        private TokenType(Token token, String icon) {
            this.token = token;
            this.icon = Images.getIcon(icon);
        }
    }
}
