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

import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.marid.groovy.GroovyRuntime;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.adapters.TextAreaWriter;
import org.marid.swing.control.ConsoleArea;
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
@PrototypeComponent
public class CommandLine extends JPanel implements GridBagLayoutSupport {

    private final GroovyShell shell = GroovyRuntime.newShell();
    private final Insets insets = new Insets(0, 0, 10, 0);
    private final ConsoleArea consoleArea = new ConsoleArea();

    public CommandLine() {
        super(new GridBagLayout());
        shell.setVariable("out", new PrintWriter(new TextAreaWriter(consoleArea)));
        add(createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        addLine(new InputArea());
    }

    public ConsoleArea getConsoleArea() {
        return consoleArea;
    }

    private void addLine(Component component) {
        add(component, gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0), getComponentCount() - 1);
        if (getParent() != null && getParent() instanceof JViewport) {
            final JViewport viewport = (JViewport) getParent();
            viewport.setViewPosition(new Point(0, Integer.MAX_VALUE));
        }
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
            final Object o = shell.evaluate(text);
            if (o != null) {
                final MetaClass metaClass = DefaultGroovyMethods.getMetaClass(o);
                final Object toString = metaClass.invokeMethod(o, "toString", new Object[0]);
                addLine(new JLabel(toString.toString()));
            }
        } catch (Exception x) {
            final StringWriter w = new StringWriter();
            try (final PrintWriter pw = new PrintWriter(w)) {
                x.printStackTrace(pw);
            }
            addLine(new JTextArea(w.toString()));
        } finally {
            area.setBracketMatchingEnabled(false);
            area.setEditable(false);
            addLine(new InputArea());
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
