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

package org.marid.ide.widgets;

import org.marid.dyn.MetaInfo;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.actions.MaridActions;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.StaticConfigurationDialog;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.SwingConstants.HORIZONTAL;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class Widget extends JInternalFrame implements WidgetSupport {

    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));
    protected final BorderLayout layout = new BorderLayout();

    public Widget(String title, Object... args) {
        super(LS.s(title, args), true, true, true, true);
        setLayout(layout);
        setName(title);
        add(toolBar, getPref("pos", BorderLayout.NORTH, "toolbar"));
        if (this instanceof Configuration) {
            toolBar.add(new MaridAction("Configuration", "settings", this::showConfigurationDialog)).setFocusable(false);
            toolBar.addSeparator();
        }
    }

    private void showConfigurationDialog(ActionEvent actionEvent) {
        final Window window = SwingUtilities.windowForComponent(this);
        new StaticConfigurationDialog(window, Widget.this.getClass()).setVisible(true);
    }

    @PostConstruct
    public void init() {
        fillActions();
        MaridActions.fillToolbar(getActionMap(), toolBar);
        if (getJMenuBar() != null) {
            MaridActions.fillMenu(getActionMap(), getJMenuBar());
        } else {
            for (final Object k : getActionMap().allKeys()) {
                final Action action = getActionMap().get(k);
                final KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
                if (stroke != null && action instanceof MaridAction) {
                    registerKeyboardAction(action, stroke, WHEN_IN_FOCUSED_WINDOW);
                }
            }
        }
        pack();
        setLocation(getPref("location", new Point()));
        setSize(getPref("size", getSize()));
    }

    @PreDestroy
    @Override
    public void dispose() {
        try {
            putPref("orientation", toolBar.getOrientation(), "toolbar");
            putPref("pos", getToolbarPosition(), "toolbar");
            if (!isMaximum()) {
                putPref("location", getLocation());
                putPref("size", getSize());
            }
        } finally {
            super.dispose();
        }
    }

    protected void fillActions() {
    }

    private String getToolbarPosition() {
        final String position = (String) layout.getConstraints(toolBar);
        return position == null ? BorderLayout.NORTH : position;
    }
}
