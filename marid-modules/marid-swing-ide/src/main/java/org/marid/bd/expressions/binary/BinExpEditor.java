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

package org.marid.bd.expressions.binary;

import org.marid.bd.components.AbstractBlockComponentEditor;
import org.marid.swing.input.ComboInputControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.marid.bd.expressions.binary.BinExpBlock.TokenType;

/**
 * @author Dmitry Ovchinnikov
 */
public class BinExpEditor extends AbstractBlockComponentEditor<BinExpBlock> {

    protected final ComboInputControl<TokenType> tokenTypeBox = new ComboInputControl<>(TokenType.class);

    public BinExpEditor(Window window, BinExpBlock block) {
        super(window, block);
        tabPane("Common").addLine("Token type", tokenTypeBox);
        tokenTypeBox.setInputValue(block.getTokenType());
        tokenTypeBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                final JLabel label = (JLabel) super.getListCellRendererComponent(l, v, i, s, f);
                label.setIcon(((TokenType) v).getIcon(16));
                return label;
            }
        });
        afterInit();
    }

    @Override
    protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
        block.setTokenType(tokenTypeBox.getInputValue());
    }
}
