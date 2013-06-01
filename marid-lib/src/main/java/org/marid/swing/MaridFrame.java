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
import org.marid.pref.PrefObject;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static org.marid.methods.LogMethods.*;
import static org.marid.methods.GuiMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MaridFrame extends JFrame implements WindowListener, PrefObject, Localized {

    private boolean firstTimeVisible = true;

    {
        addWindowListener(this);
    }

    public MaridFrame() {
    }

    public MaridFrame(String title) {
        super(S.l(title));
    }

    @Override
    public void windowOpened(WindowEvent e) {
        setState(getPreferences(this).getInt("state", getState()));
        setExtendedState(getPreferences(this).getInt("extendedState", getExtendedState()));
    }

    @Override
    public void windowClosing(WindowEvent e) {
        putPoint(getPreferences(this), "location", getLocation());
        putDimension(getPreferences(this), "size", getSize());
        getPreferences(this).putInt("state", getState());
        getPreferences(this).putInt("extendedState", getExtendedState());
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
        getPreferences(this).putInt("state", getState());
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        getPreferences(this).putInt("state", getState());
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void setVisible(boolean b) {
        if (firstTimeVisible) {
            firstTimeVisible = false;
            setLocation(getPoint(getPreferences(this), "location", getLocation()));
            setSize(getDimension(getPreferences(this), "size", getPreferredSize()));
        }
        super.setVisible(b);
    }
}
