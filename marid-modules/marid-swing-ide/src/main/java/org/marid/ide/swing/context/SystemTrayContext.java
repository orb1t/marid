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

package org.marid.ide.swing.context;

import images.Images;
import org.marid.ide.base.IdeFrame;
import org.marid.image.MaridIcon;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.log.TrayIconHandler;
import org.marid.swing.menu.MenuActionTreeElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;
import java.util.logging.Level;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class SystemTrayContext implements SysPrefSupport {

    @Autowired
    MenuActionTreeElement ideMenuActionTreeElement;

    @Autowired
    IdeFrame ideFrame;

    @Bean
    public TrayIcon ideTrayIcon() throws AWTException {
        if (!SystemTray.isSupported()) {
            return new TrayIcon(Images.getEmptyImage(16, 16));
        }
        final SystemTray tray = SystemTray.getSystemTray();
        final Dimension traySize = tray.getTrayIconSize();
        final int trayWidth = traySize.width;
        final int trayHeight = traySize.height;
        final Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
        final PopupMenu popupMenu = new PopupMenu();
        final MenuItem showLogMenuItem = new MenuItem(s("Show log..."));
        showLogMenuItem.addActionListener(e -> ideFrame.showLog());
        popupMenu.add(showLogMenuItem);
        popupMenu.addSeparator();
        final MenuItem exitMenuItem = new MenuItem(s("Exit"));
        exitMenuItem.addActionListener(e -> ideFrame.exitWithConfirm());
        popupMenu.add(exitMenuItem);
        popupMenu.addSeparator();
        ideMenuActionTreeElement.fillPopupMenu(popupMenu);
        final TrayIcon icon = new TrayIcon(image, s("Marid IDE"), popupMenu);
        icon.addActionListener(ev -> ideFrame.setVisible(!ideFrame.isVisible()));
        ideFrame.setVisible(true);
        tray.add(icon);
        TrayIconHandler.addSystemHandler(icon, Level.parse(getSysPref("trayLevel", Level.OFF.getName())));
        return icon;
    }
}
