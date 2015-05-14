/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.gui;

import org.marid.image.MaridIcon;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.actions.WindowAction;
import org.marid.swing.log.TrayIconHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import static java.util.logging.Level.OFF;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MaridTrayIconImpl implements AutoCloseable, LogSupport, L10nSupport, SysPrefSupport {

    private final TrayIcon trayIcon;

    @Autowired
    public MaridTrayIconImpl(IdeFrameImpl ideFrame) throws Exception {
        if (SystemTray.getSystemTray() != null) {
            final Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
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
            ideFrame.addWindowListener(new WindowAction(e -> {
                switch (e.getID()) {
                    case WindowEvent.WINDOW_OPENED:
                        for (int i = 0; i < ideFrame.getJMenuBar().getMenuCount(); i++) {
                            popupMenu.add(menu(ideFrame.getJMenuBar().getMenu(i)));
                        }
                        break;
                }
            }));
            final TrayIcon icon = new TrayIcon(image, s("Marid IDE"), popupMenu);
            icon.addActionListener(ev -> ideFrame.setVisible(!ideFrame.isVisible()));
            ideFrame.setVisible(true);
            SystemTray.getSystemTray().add(icon);
            TrayIconHandler.addSystemHandler(icon, Level.parse(getSysPref("trayLevel", OFF.getName())));
            trayIcon = icon;
        } else {
            trayIcon = null;
        }
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    @Override
    public void close() throws Exception {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    private Menu menu(JMenu menu) {
        final Menu m = new Menu(menu.getText());
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            final Object menuComponent = menu.getMenuComponent(i);
            if (menuComponent instanceof JCheckBoxMenuItem) {
                final JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuComponent;
                final CheckboxMenuItem it = new CheckboxMenuItem(item.getText(), item.getState());
                if (item.getAction() != null) {
                    item.getAction().addPropertyChangeListener(evt -> {
                        switch (evt.getPropertyName()) {
                            case Action.SELECTED_KEY:
                                it.setState((boolean) item.getAction().getValue(Action.SELECTED_KEY));
                                break;
                            case "enabled":
                                it.setEnabled(item.getAction().isEnabled());
                                break;
                        }
                    });
                    it.addActionListener(item.getAction());
                }
                m.add(it);
            } else if (menuComponent instanceof JMenu) {
                m.add(menu((JMenu) menuComponent));
            } else if (menuComponent instanceof JMenuItem) {
                final JMenuItem item = (JMenuItem) menuComponent;
                final MenuItem it = new MenuItem(item.getText());
                if (item.getAction() != null) {
                    item.getAction().addPropertyChangeListener(evt -> {
                        switch (evt.getPropertyName()) {
                            case "enabled":
                                it.setEnabled(item.getAction().isEnabled());
                                break;
                        }
                    });
                    it.addActionListener(item.getAction());
                }
                m.add(it);
            }
        }
        return m;
    }
}
