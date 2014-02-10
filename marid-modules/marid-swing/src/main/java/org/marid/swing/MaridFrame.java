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

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import static org.marid.l10n.L10n.s;
import static org.marid.swing.methods.GuiMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MaridFrame extends JFrame {

    public MaridFrame(String title) {
        super(s(title));
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(prefNode().getInt("state", getState()));
                setExtendedState(prefNode().getInt("extendedState", getExtendedState()));
                setLocation(getPoint(prefNode(), "location", getLocation()));
                setSize(getDimension(prefNode(), "size", getPreferredSize()));
                break;
            case WindowEvent.WINDOW_CLOSED:
                putPoint(prefNode(), "location", getLocation());
                putDimension(prefNode(), "size", getSize());
                prefNode().putInt("state", getState());
                prefNode().putInt("extendedState", getExtendedState());
                break;
            case WindowEvent.WINDOW_ICONIFIED:
                prefNode().putInt("state", getState());
                break;
            case WindowEvent.WINDOW_DEICONIFIED:
                prefNode().putInt("state", getState());
                break;
        }
        super.processWindowEvent(e);
    }

    protected abstract Preferences prefNode();
}
