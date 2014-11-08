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
import org.marid.ide.swing.gui.IdeFrameImpl;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.actions.ActionKeySupport;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.actions.MaridActions;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.StaticConfigurationDialog;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

import static javax.swing.SwingConstants.HORIZONTAL;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class Widget extends JInternalFrame implements PrefSupport, L10nSupport, LogSupport, ActionKeySupport {

    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));
    protected final IdeFrameImpl owner;
    protected final BorderLayout layout = new BorderLayout();

    public Widget(GenericApplicationContext context, String title, Object... args) {
        super(LS.s(title, args), true, true, true, true);
        owner = context.getBean(IdeFrameImpl.class);
        setLayout(layout);
        setName(title);
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (isSingleton()) {
                    hide();
                } else {
                    context.getAutowireCapableBeanFactory().destroyBean(Widget.this);
                }
            }
        });
        add(toolBar, getPref("pos", BorderLayout.NORTH, "toolbar"));
        if (this instanceof Configuration) {
            toolBar.add(new MaridAction("Configuration", "settings", e ->
                    new StaticConfigurationDialog(owner, Widget.this.getClass()).setVisible(true))).setFocusable(false);
            toolBar.addSeparator();
        }
    }

    public boolean isSingleton() {
        return getClass().isAnnotationPresent(SingletonWidget.class);
    }

    @Override
    public void pack() {
        fillActions();
        MaridActions.fillToolbar(getActionMap(), toolBar);
        super.pack();
    }

    @PostConstruct
    public void init() {
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
