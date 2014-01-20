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

package org.marid.ide.swing;

import org.marid.ide.menu.GroovyMenu;
import org.marid.ide.menu.MenuEntry;
import org.marid.image.MaridIcon;
import org.marid.l10n.Localized;
import org.marid.swing.log.TrayIconHandler;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingIde {

    static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    static final Preferences SYSPREFS = preferences("system");
    public static SwingIdeFrame frame;

    public static void run() {
        installLaf();
        final List<MenuEntry> entries = GroovyMenu.getMenuEntries();
        if (SystemTray.isSupported()) {
            try {
                final SystemTray tray = SystemTray.getSystemTray();
                final Dimension traySize = tray.getTrayIconSize();
                final int trayWidth = traySize.width;
                final int trayHeight = traySize.height;
                final Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
                final PopupMenuImpl popup = new PopupMenuImpl(entries);
                final TrayIcon icon = new TrayIcon(image, Localized.S.l("Marid IDE"), popup);
                icon.addActionListener(popup);
                icon.setActionCommand("show_hide");
                frame = new SwingIdeFrame(true, entries);
                frame.setVisible(true);
                tray.add(icon);
                TrayIconHandler.addSystemHandler(icon, Level.parse(SYSPREFS.get("trayLevel", Level.OFF.getName())));
            } catch (Exception x) {
                warning(LOG, "Unable to create the tray icon", x);
                frame = new SwingIdeFrame(false, entries);
                frame.setVisible(true);
            }
        }
    }

    private static void installLaf() {
        final String laf = SYSPREFS.get("laf", null);
        try {
            if (laf != null) {
                UIManager.setLookAndFeel(laf);
            } else {
                UIManager.setLookAndFeel(new NimbusLookAndFeel());
            }
        } catch (Exception x) {
            warning(LOG, "Unable to set LAF {0}", x, laf);
        }
    }
}
