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

package org.marid.swing.input;

import org.marid.l10n.L10n;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class StringArrayInputControl extends AbstractTitledPanel<String[]> {

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JToolBar toolBar;

    public StringArrayInputControl(String toolbarPosition) {
        setLayout(new BorderLayout());
        add(toolBar = new JToolBar(getToolbarOrientation(toolbarPosition)), getToolbarPosition(toolbarPosition));
        toolBar.setBorderPainted(false);
        toolBar.setFloatable(false);
        add(new JScrollPane(list));
        final Action removeAction = new MaridAction("Remove", "remove", (a, e) -> {
            if (list.getSelectedIndex() >= 0) {
                model.remove(list.getSelectedIndex());
            }
        });
        toolBar.add(new MaridAction("Add", "add", (a, e) -> {
            final String item = JOptionPane.showInputDialog(this, L10n.m("Input a value") + ":");
            if (item != null) {
                model.addElement(item.trim());
            }
        })).setFocusable(false);
        toolBar.add(removeAction).setFocusable(false);
        toolBar.add(new MaridAction("Clear", "clear", (a, e) -> {
            model.clear();
        })).setFocusable(false);
        toolBar.addSeparator();
        list.registerKeyboardAction(removeAction, KeyStroke.getKeyStroke("DELETE"), JComponent.WHEN_FOCUSED);
    }

    public StringArrayInputControl() {
        this(BorderLayout.NORTH);
    }

    public StringArrayInputControl withToolbar(BiConsumer<DefaultListModel<String>, JToolBar> consumer) {
        consumer.accept(model, toolBar);
        return this;
    }

    private int getToolbarOrientation(String toolbarPosition) {
        switch (toolbarPosition) {
            case BorderLayout.NORTH:
            case BorderLayout.SOUTH:
                return JToolBar.HORIZONTAL;
            case BorderLayout.WEST:
            case BorderLayout.EAST:
                return JToolBar.VERTICAL;
            default:
                throw new IllegalArgumentException(toolbarPosition);
        }
    }

    private String getToolbarPosition(String toolbarPosition) {
        switch (toolbarPosition) {
            case BorderLayout.NORTH:
            case BorderLayout.SOUTH:
            case BorderLayout.WEST:
            case BorderLayout.EAST:
                return toolbarPosition;
            default:
                throw new IllegalArgumentException(toolbarPosition);
        }
    }

    @Override
    public String[] getValue() {
        final String[] array = new String[model.getSize()];
        for (int i = 0; i < array.length; i++) {
            array[i] = model.get(i);
        }
        return array;
    }

    @Override
    public void setValue(String[] value) {
        model.clear();
        for (final String s : value) {
            model.addElement(s);
        }
    }
}
