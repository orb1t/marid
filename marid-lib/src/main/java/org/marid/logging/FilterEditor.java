/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.logging;

import groovy.ui.ConsoleTextEditor;
import org.marid.l10n.Localized;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class FilterEditor extends JDialog implements Localized {

    private final ConsoleTextEditor editor;
    private final Action accept = new AbstractAction(S.l("Accept")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            filter = editor.getTextEditor().getText();
            dispose();
        }
    };
    private final Action reject = new AbstractAction(S.l("Reject")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    };
    private String filter;

    public FilterEditor(JFrame frame) {
        super(frame, S.l("Filter editor"), true);
        ((BorderLayout)getLayout()).setHgap(10);
        add(editor = new ConsoleTextEditor());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(new JButton(accept), BorderLayout.EAST);
        buttonPanel.add(new JButton(reject), BorderLayout.WEST);
        add(buttonPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(400, 300));
        pack();
        setLocationRelativeTo(frame);
    }

    public String getFilter() {
        return filter;
    }
}
