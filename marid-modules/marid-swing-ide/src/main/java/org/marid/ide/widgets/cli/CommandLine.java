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
import org.marid.collections.history.HistoryNavigator;
import org.marid.groovy.GroovyRuntime;
import org.marid.pref.PrefSupport;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.actions.ActionKeySupport;
import org.marid.swing.adapters.TextAreaWriter;
import org.marid.swing.control.ConsoleArea;
import org.marid.swing.layout.GridBagLayoutSupport;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.awt.GridBagConstraints.*;
import static javax.swing.Box.createVerticalGlue;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class CommandLine extends JPanel implements GridBagLayoutSupport, PrefSupport, ActionKeySupport {

    private final GroovyShell shell = GroovyRuntime.newShell();
    private final Insets insets = new Insets(0, 0, 10, 0);
    private final ConsoleArea consoleArea = new ConsoleArea();
    private final HistoryNavigator<String> history;

    private boolean autoClean = getPref("autoClean", true);

    public CommandLine() {
        super(new GridBagLayout());
        shell.setVariable("out", new PrintWriter(new TextAreaWriter(consoleArea)));
        add(createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        addLine(new InputArea());
        history = new HistoryNavigator<>(String.class, 1000, e -> {
            final String v = e.trim();
            return v.isEmpty() ? null : v;
        });
        history.getHistory().load(preferences());
    }

    @PreDestroy
    protected void destroy() {
        history.getHistory().save(preferences());
    }

    public ConsoleArea getConsoleArea() {
        return consoleArea;
    }

    public boolean isAutoClean() {
        return autoClean;
    }

    public void setAutoClean(boolean autoClean) {
        this.autoClean = autoClean;
    }

    public void clear() {
        final Component[] components = getComponents();
        for (int i = components.length - 3; i >= 0; i--) {
            remove(components[i]);
        }
        validate();
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

    private void evaluate(InputArea area) {
        final String text = area.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            if (autoClean) {
                consoleArea.setText("");
            }
            final Object o = shell.evaluate(text);
            if (o != null) {
                final MetaClass metaClass = DefaultGroovyMethods.getMetaClass(o);
                final Object toString = metaClass.invokeMethod(o, "toString", new Object[0]);
                addLine(new JLabel(toString.toString()));
            }
            history.add(text);
        } catch (Exception x) {
            final StringWriter w = new StringWriter();
            try (final PrintWriter pw = new PrintWriter(w)) {
                x.printStackTrace(pw);
            }
            addLine(new JTextArea(w.toString()));
        } finally {
            area.shutdown();
            addLine(new InputArea());
        }
    }

    private class InputArea extends RSyntaxTextArea {

        private String selectedValue;

        private InputArea() {
            super(new RSyntaxDocument(SYNTAX_STYLE_GROOVY));
            setAnimateBracketMatching(true);
            setAntiAliasingEnabled(true);
            setHighlightCurrentLine(false);
            registerKeyboardAction(e -> evaluate(this), getKeyStroke("control ENTER"), WHEN_FOCUSED);
            registerKeyboardAction(e -> previous(), getKeyStroke("control UP"), WHEN_FOCUSED);
            registerKeyboardAction(e -> next(), getKeyStroke("control DOWN"), WHEN_FOCUSED);
            registerKeyboardAction(e -> reset(), getKeyStroke("ESCAPE"), WHEN_FOCUSED);
        }

        private void processSelectedValue() {
            if (selectedValue == null) {
                final String v = history.getHistory().getAddOp().apply(getText());
                if (v != null && !history.getHistory().containsItem(v)) {
                    selectedValue = v;
                }
            }
        }

        private void previous() {
            processSelectedValue();
            final String value = history.getPrevious();
            if (value != null) {
                setText(value);
            }
        }

        private void next() {
            processSelectedValue();
            final String value = history.getNext();
            if (value != null) {
                setText(value);
            }
        }

        private void reset() {
            setText(selectedValue != null ? selectedValue : "");
            selectedValue = null;
            history.reset();
        }

        private void shutdown() {
            selectedValue = null;
            setBracketMatchingEnabled(false);
            setEditable(false);
            getActionMap().clear();
            getInputMap().clear();
            history.reset();
        }
    }
}
