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

package org.marid.swing.component;

import images.Images;
import org.marid.swing.actions.GenericAction;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ResizablePanel<T extends JComponent> extends JPanel {

    protected final T component;

    public ResizablePanel(T component) {
        super(new BorderLayout());
        add(this.component = component);
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        final JButton button = new JButton(new GenericAction(Images.getIcon("close.png"), e -> setVisible(false)));
        button.setFocusable(false);
        add(buttonPanel, BorderLayout.NORTH);
    }
}
