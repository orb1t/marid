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

package org.marid.ide.impl

import org.marid.ide.Ide
import org.marid.ide.itf.Frame
import org.marid.ide.menu.MenuBar

import javax.swing.*
import java.awt.*
import java.util.prefs.Preferences

/**
 * Application frame implementation.
 *
 * @author Dmitry Ovchinnikov 
 */
class FrameImpl extends JFrame implements Frame {

    private final def application;
    private final def preferences = Preferences.userNodeForPackage(Ide).node("frame");

    FrameImpl(ApplicationImpl application) {
        this.application = application;
        defaultCloseOperation = EXIT_ON_CLOSE;
        preferredSize = new Dimension(400, 300);
        setJMenuBar(new MenuBar(application.menuEntries.take()));
        pack();
    }

    @Override
    Preferences getPreferences() {
        return preferences;
    }
}
