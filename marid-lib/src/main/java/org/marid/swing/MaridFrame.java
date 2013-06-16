/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

import org.marid.l10n.Localized;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static org.marid.methods.GuiMethods.*;
import static org.marid.methods.PrefMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MaridFrame extends JFrame implements WindowListener,Localized {

    private static final long serialVersionUID = 8281239938628777661L;

    public MaridFrame(String title) {
        super(S.l(title));
        addWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        setState(preferences(prefNode()).getInt("state", getState()));
        setExtendedState(preferences(prefNode()).getInt("extendedState", getExtendedState()));
    }

    @Override
    public void windowClosing(WindowEvent e) {
        putPoint(preferences(prefNode()), "location", getLocation());
        putDimension(preferences(prefNode()), "size", getSize());
        preferences(prefNode()).putInt("state", getState());
        preferences(prefNode()).putInt("extendedState", getExtendedState());
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
        preferences(prefNode()).putInt("state", getState());
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        preferences(prefNode()).putInt("state", getState());
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void setVisible(boolean b) {
        if (!prefNode().equals(getName())) {
            setName(prefNode());
            setLocation(getPoint(preferences(prefNode()), "location", getLocation()));
            setSize(getDimension(preferences(prefNode()), "size", getPreferredSize()));
        }
        super.setVisible(b);
    }

    protected abstract String prefNode();
}
