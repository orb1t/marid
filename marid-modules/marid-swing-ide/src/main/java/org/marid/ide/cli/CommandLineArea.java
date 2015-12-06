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

import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class CommandLineArea extends JTextArea implements LogSupport {

    public CommandLineArea() {
        super(1, 0);
        setLineWrap(true);
        setForeground(SystemColor.controlLtHighlight);
        setBackground(SystemColor.controlShadow);
        setCaretColor(SystemColor.controlLtHighlight);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    public void reset() {
        setText("");
        setRows(1);
    }
}
