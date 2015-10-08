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
import org.marid.swing.actions.MaridAction;
import org.marid.swing.actions.MaridActions;
import org.marid.swing.actions.MouseAction;
import org.marid.swing.log.TrayIconHandler;
import org.marid.swing.menu.SwingPopupMenuWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MaridTrayIconImpl implements AutoCloseable, LogSupport, L10nSupport, SysPrefSupport {

    private final TrayIcon trayIcon;

    @Autowired
    public MaridTrayIconImpl(IdeFrameImpl ideFrame, ActionMap ideActionMap) throws Exception {
        if (SystemTray.getSystemTray() != null) {
            final Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
            final int trayWidth = traySize.width;
            final int trayHeight = traySize.height;
            final Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
            final JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new MaridAction("Show log...", "log", e -> ideFrame.showLog()));
            popupMenu.addSeparator();
            popupMenu.add(new MaridAction("Exit", "exit", e -> ideFrame.exitWithConfirm()));
            popupMenu.addSeparator();
            final TrayIcon icon = new TrayIcon(image, s("Marid IDE"), null);
            icon.addActionListener(ev -> ideFrame.setVisible(!ideFrame.isVisible()));
            final Color transparentColor = new Color(0, 0, 0, 0);
            icon.addMouseListener(new MouseAction(e -> {
                switch (e.getID()) {
                    case MouseEvent.MOUSE_RELEASED:
                    case MouseEvent.MOUSE_PRESSED:
                        if (e.isPopupTrigger()) {
                            if (icon.getActionCommand() == null) {
                                try {
                                    MaridActions.fillMenu(ideActionMap, new SwingPopupMenuWrapper(popupMenu));
                                } finally {
                                    icon.setActionCommand("maridTray");
                                }
                            }
                            final Frame frame = new Frame();
                            frame.setUndecorated(true);
                            frame.setBackground(transparentColor);
                            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                            final WindowAdapter windowAdapter = new WindowAdapter() {
                                @Override
                                public void windowLostFocus(WindowEvent e) {
                                    frame.dispose();
                                }

                                @Override
                                public void windowClosed(WindowEvent e) {
                                    log(INFO, "Internal popup window closed");
                                }
                            };
                            frame.addWindowFocusListener(windowAdapter);
                            frame.addWindowListener(windowAdapter);
                            popupMenu.addPopupMenuListener(new PopupMenuListener() {
                                @Override
                                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                }

                                @Override
                                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                    popupMenu.removePopupMenuListener(this);
                                    frame.dispose();
                                }

                                @Override
                                public void popupMenuCanceled(PopupMenuEvent e) {
                                    popupMenu.removePopupMenuListener(this);
                                    frame.dispose();
                                }
                            });
                            frame.setVisible(true);
                            popupMenu.show(frame, e.getX(), e.getY());
                        }
                        break;
                }
            }));
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
}
