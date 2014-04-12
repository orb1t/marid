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
import org.marid.ide.IdeFrame;
import org.marid.pref.PrefSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.ConfigurationDialog;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

import static javax.swing.SwingConstants.HORIZONTAL;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class Widget extends JInternalFrame implements PrefSupport {

    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));
    protected final IdeFrame owner;

    public Widget(IdeFrame owner, String title) {
        super(s(title));
        this.owner = owner;
        setName(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(toolBar, getPref("pos", BorderLayout.NORTH, "toolbar"));
        if (this instanceof Configuration) {
            toolBar.add(new MaridAction("Configuration", "settings", e -> new ConfigurationDialog<>(
                    (Component & Configuration & PrefSupport) this).setVisible(true))).setFocusable(false);
            toolBar.addSeparator();
        }
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                putPref("orientation", toolBar.getOrientation(), "toolbar");
                putPref("pos", getToolbarPosition(), "toolbar");
                if (!isMaximum()) {
                    putPref("location", getLocation());
                    putPref("size", getSize());
                }
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                setSize(getPref("size", getSize()));
            }
        });
        setLocation(getPref("location", new Point()));
    }

    private String getToolbarPosition() {
        final String position = (String) ((BorderLayout) getLayout()).getConstraints(toolBar);
        return position == null ? BorderLayout.NORTH : position;
    }
}
