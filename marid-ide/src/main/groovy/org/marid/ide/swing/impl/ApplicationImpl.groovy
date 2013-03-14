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
import org.marid.ide.itf.Application
import org.marid.ide.menu.MaridMenu
import org.marid.ide.menu.MenuEntry
import org.marid.ide.swing.util.ImageGenDialog
import org.marid.ide.swing.util.LafSelectionDialog
import org.marid.image.MaridIcon

import javax.swing.*
import javax.swing.plaf.nimbus.NimbusLookAndFeel
import java.awt.*
import java.util.List
import java.util.concurrent.SynchronousQueue

@Log
class ApplicationImpl implements Application {

    private FrameImpl frame;

    ApplicationImpl() {
        def menuEntries = new SynchronousQueue<List<MenuEntry>>();
        Thread.start {
            def entries = new ArrayList<MenuEntry>();
            def sl = ServiceLoader.load(MaridMenu, new GroovyClassLoader());
            for (def menu in sl) {
                entries.addAll(menu.menuEntries);
            }
            menuEntries.put(entries);
        }
        def laf = preferences.get("laf", NimbusLookAndFeel.name);
        try {
            UIManager.setLookAndFeel(laf);
        } catch (x) {
            log.warning("{0} error", x, laf)
        }
        EventQueue.invokeLater {
            def entries = menuEntries.take();
            frame = new FrameImpl(this, entries);
            frame.visible = true;
            initTray(entries);
        };
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

    private void initTray(List<MenuEntry> entries) {
        if (SystemTray.supported) {
            def tray = SystemTray.systemTray;
            def traySize = tray.trayIconSize;
            def trayWidth = (int)traySize.width;
            def trayHeight = (int)traySize.height;
            def image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
            def popup = new PopupMenuImpl(this, entries);
            def icon = new TrayIcon(image, "Marid IDE".ls(), popup);
            icon.addActionListener(popup);
            tray.add(icon);
        }
    }

    @Override
    String getIdeObjectType() {
        return "application";
    }
}
