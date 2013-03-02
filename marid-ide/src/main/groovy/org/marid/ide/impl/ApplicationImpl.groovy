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
import org.marid.ide.itf.Application
import org.marid.ide.itf.Frame
import org.marid.ide.menu.MaridMenu
import org.marid.ide.menu.MenuEntry

import java.awt.EventQueue
import java.util.concurrent.SynchronousQueue
import java.util.prefs.Preferences

class ApplicationImpl implements Application, Runnable {

    private final def preferences = Preferences.userNodeForPackage(Ide).node("application");
    protected final def menuEntries = new SynchronousQueue<List<MenuEntry>>();
    private frame;

    ApplicationImpl() {
        Thread.start {
            def entries = new ArrayList<MenuEntry>();
            def sl = ServiceLoader.load(MaridMenu, new GroovyClassLoader());
            for (def menu in sl) {
                entries.addAll(menu.menuEntries);
            }
            menuEntries.put(entries);
        }
        EventQueue.invokeLater(this);
    }

    @Override
    String getVersion() {
        return getClass().package.implementationVersion;
    }

    @Override
    void exit() {
        System.exit(0);
    }

    @Override
    Frame getFrame() {
        return frame;
    }

    @Override
    Preferences getPreferences() {
        return preferences;
    }

    @Override
    void run() {
        frame = new FrameImpl(this);
        frame.visible = true;
    }
}
