/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.cli;

import org.marid.swing.actions.MaridAction;
import org.marid.swing.component.SmallButton;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class CommandLineResult extends JPanel {

    private static final Color BACKGROUND = new Color(0, 0, 0, 0);

    private final JTextArea textArea;

    public CommandLineResult(String text) {
        super(new BorderLayout(10, 0));
        setOpaque(false);
        add(textArea = new JTextArea(text));
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setBackground(BACKGROUND);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setForeground(SystemColor.controlLtHighlight);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        add(new SmallButton(new MaridAction(null, "remove.png", e -> {
            final Container container = getParent();
            container.remove(this);
            container.validate();
        }, Action.SHORT_DESCRIPTION, "Remove")), BorderLayout.EAST);
    }
}
