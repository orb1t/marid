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

package org.marid.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class OutputArea extends JTextArea {

    private JScrollPane scrollPane;

    public OutputArea(String text, int rows) {
        super(text, rows, 0);
        setBackground(SystemColor.controlDkShadow);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setForeground(SystemColor.controlLtHighlight);
    }

    public OutputArea(int rows) {
        this(null, rows);
    }

    public JScrollPane getScrollPane() {
        return scrollPane != null ? scrollPane : (scrollPane = new JScrollPane(this,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }
}
