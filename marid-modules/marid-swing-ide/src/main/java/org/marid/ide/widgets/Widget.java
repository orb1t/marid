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
import org.marid.swing.MaridAction;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.StaticConfigurationDialog;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingConstants.HORIZONTAL;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class Widget extends JInternalFrame implements PrefSupport, L10nSupport, LogSupport {

    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));
    protected final IdeFrameImpl owner;
    protected final BorderLayout layout = new BorderLayout();

    public Widget(IdeFrameImpl owner, String title, Object... args) {
        super(LS.s(title, args), true, true, true, true);
        this.owner = owner;
        setLayout(layout);
        setName(title);
        setDefaultCloseOperation(isSingleton() ? HIDE_ON_CLOSE : DISPOSE_ON_CLOSE);
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

    @PostConstruct
    public void init() {
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

    private String getToolbarPosition() {
        final String position = (String) layout.getConstraints(toolBar);
        return position == null ? BorderLayout.NORTH : position;
    }
}
