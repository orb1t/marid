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

package org.marid.ide.swing.impl

import groovy.util.logging.Log
import org.marid.ide.Ide
import org.marid.ide.itf.Application
import org.marid.ide.menu.MaridMenu
import org.marid.ide.menu.MenuEntry
import org.marid.ide.swing.util.ImageGenDialog
import org.marid.ide.swing.util.LafSelectionDialog

import javax.swing.*
import java.awt.*
import java.util.List
import java.util.concurrent.SynchronousQueue
import java.util.prefs.Preferences

@Log
class ApplicationImpl implements Application, Runnable {

    private final def preferences = Preferences.userNodeForPackage(Ide).node("application");
    protected final def menuEntries = new SynchronousQueue<List<MenuEntry>>();
    private FrameImpl frame;

    ApplicationImpl() {
        Thread.start {
            def entries = new ArrayList<MenuEntry>();
            def sl = ServiceLoader.load(MaridMenu, new GroovyClassLoader());
            for (def menu in sl) {
                entries.addAll(menu.menuEntries);
            }
            menuEntries.put(entries);
        }
        def laf = preferences.get("laf", UIManager.getCrossPlatformLookAndFeelClassName());
        try {
            UIManager.setLookAndFeel(laf as String);
        } catch (x) {
            log.warning("{0} error", x, laf)
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
    FrameImpl getFrame() {
        return frame;
    }

    @Override
    void showImageGenDialog() {
        new ImageGenDialog(frame, "Marid image generation".ls(), false).visible = true;
    }

    @Override
    void showLafSelectionDialog() {
        new LafSelectionDialog(frame, "LAF selection".ls(), true).visible = true;
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
