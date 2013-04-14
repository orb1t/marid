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

package org.marid.swing

import groovy.transform.InheritConstructors
import org.marid.itf.GuiObject

import javax.swing.*
import java.awt.event.WindowEvent
import java.awt.event.WindowListener

/**
 * Marid frame.
 *
 * @author Dmitry Ovchinnikov 
 */
@InheritConstructors
abstract class MaridFrame extends JFrame implements WindowListener, GuiObject {

    private boolean firstTimeVisible = true;

    {
        addWindowListener(this);
    }

    @Override
    void windowOpened(WindowEvent e) {
        state = preferences.getInt("state", state);
        extendedState = preferences.getInt("extendedState", extendedState);
    }

    @Override
    void windowClosing(WindowEvent e) {
        preferences.putPoint("location", location);
        preferences.putDimension("size", size);
        preferences.putInt("state", state);
        preferences.putInt("extendedState", extendedState);
    }

    @Override
    void windowClosed(WindowEvent e) {
    }

    @Override
    void windowIconified(WindowEvent e) {
        preferences.putInt("state", state);
    }

    @Override
    void windowDeiconified(WindowEvent e) {
        preferences.putInt("state", state);
    }

    @Override
    void windowActivated(WindowEvent e) {
    }

    @Override
    void windowDeactivated(WindowEvent e) {
    }

    @Override
    void setVisible(boolean b) {
        if (firstTimeVisible) {
            firstTimeVisible = false;
            location = preferences.getPoint("location", location);
            size = preferences.getDimension("size", preferredSize);
        }
        super.setVisible(b)
    }
}
