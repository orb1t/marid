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

package org.marid.swing;

import org.marid.pref.PrefSupport;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.prefs.Preferences;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractInternalFrame<F extends AbstractMultiFrame> extends JInternalFrame implements PrefSupport {

    protected final F owner;
    protected final Preferences preferences;

    protected AbstractInternalFrame(F owner, String name, String title, boolean closable) {
        super(s(title), true, closable, true, true);
        this.owner = owner;
        preferences = owner.preferences().node("frames").node(name);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(getPref("size", getInitialSize()));
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                try {
                    putPref("maximized", isMaximum());
                    if (!isMaximum()) {
                        putPref("location", getLocation());
                        putPref("size", getSize());
                    }
                } finally {
                    removeInternalFrameListener(this);
                }
            }
        });
    }

    @Override
    public void show() {
        try {
            setMaximum(getPref("maximized", isMaximum()));
        } catch (PropertyVetoException x) {
            throw new IllegalStateException(x);
        }
        if (!isMaximum()) {
            setLocation(getPref("location", getInitialLocation()));
        }
        super.show();
    }

    @Override
    public Preferences preferences() {
        return preferences;
    }

    protected Dimension getInitialSize() {
        return new Dimension(500, 400);
    }

    protected Point getInitialLocation() {
        return new Point(0, 0);
    }
}
