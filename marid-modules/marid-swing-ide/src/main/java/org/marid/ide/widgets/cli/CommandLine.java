/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.widgets.cli;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.marid.groovy.GroovyRuntime;
import org.marid.swing.layout.GridBagLayoutSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.awt.GridBagConstraints.*;
import static javax.swing.Box.createVerticalGlue;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * @author Dmitry Ovchinnikov
 */
public class CommandLine extends JPanel implements GridBagLayoutSupport {

    private final Insets insets = new Insets(0, 0, 10, 0);

    public CommandLine() {
        super(new GridBagLayout());
        add(createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        addLine(new InputArea());
    }

    private void addLine(Component component) {
        add(component, gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0), getComponentCount() - 1);
        if (component instanceof InputArea) {
            component.requestFocus();
        }
    }

    private void evaluate(ActionEvent actionEvent) {
        final InputArea area = (InputArea) actionEvent.getSource();
        final String text = area.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            final Object o = GroovyRuntime.SHELL.evaluate(text);
            area.setBracketMatchingEnabled(false);
            area.setEditable(false);
            setBackground(SystemColor.control);
            addLine(new JLabel(String.valueOf(o)));
            addLine(new InputArea());
        } catch (Exception x) {
            final StringWriter w = new StringWriter();
            try (final PrintWriter pw = new PrintWriter(w)) {
                x.printStackTrace(pw);
            }
            addLine(new JLabel(w.toString()));
        }
    }

    private class InputArea extends RSyntaxTextArea {

        private InputArea() {
            super(new RSyntaxDocument(SYNTAX_STYLE_GROOVY));
            setAnimateBracketMatching(true);
            setAntiAliasingEnabled(true);
            setHighlightCurrentLine(false);
            registerKeyboardAction(CommandLine.this::evaluate, getKeyStroke("control ENTER"), WHEN_FOCUSED);
        }
    }
}
