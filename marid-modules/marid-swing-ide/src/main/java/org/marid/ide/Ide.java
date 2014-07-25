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

package org.marid.ide;

import groovy.lang.GroovyCodeSource;
import org.marid.groovy.GroovyRuntime;
import org.marid.image.MaridIcon;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.log.TrayIconHandler;
import org.marid.swing.menu.MenuAction;
import org.marid.swing.menu.MenuActionList;
import org.marid.swing.menu.MenuActionTreeElement;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.marid.dyn.TypeCaster.TYPE_CASTER;
import static org.marid.l10n.L10n.s;
import static org.marid.methods.LogMethods.warning;
import static org.marid.swing.MaridAction.MaridActionListener;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide implements SysPrefSupport {

    static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    public static IdeFrame frame;

    public static void run() {
        installLaf();
        final MenuActionList menuActions = new MenuActionList();
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (final Enumeration<URL> e = cl.getResources("menu/Menu.groovy"); e.hasMoreElements();) {
                final URL url = e.nextElement();
                try {
                    final List list = (List) GroovyRuntime.SHELL.evaluate(new GroovyCodeSource(url));
                    for (final Object le : list) {
                        if (!(le instanceof List)) {
                            continue;
                        }
                        final List l = (List) le;
                        final String[] path;
                        final String group;
                        final String name;
                        final String icon;
                        final MaridActionListener mal;
                        final Object[] args;
                        switch (l.size()) {
                            case 6:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = TYPE_CASTER.cast(String.class, l.get(3));
                                mal = TYPE_CASTER.cast(MaridActionListener.class, l.get(4));
                                args = TYPE_CASTER.cast(Object[].class, l.get(5));
                                break;
                            case 5:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = TYPE_CASTER.cast(String.class, l.get(3));
                                mal = TYPE_CASTER.cast(MaridActionListener.class, l.get(4));
                                args = new String[0];
                                break;
                            case 3:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = null;
                                mal = null;
                                args = null;
                                break;
                            default:
                                warning(LOG, "Invalid menu line: {0}", l);
                                continue;
                        }
                        final MaridAction action = mal == null ? null : new MaridAction(s(name), icon, mal, args);
                        menuActions.add(new MenuAction(name, group, path, action));
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to load menu entries from {0}", x, url);
                }
            }
        } catch (Exception x) {
            warning(LOG, "Unable to load menu entries", x);
        }
        final MenuActionTreeElement menuRoot = menuActions.createTreeElement();
        if (SystemTray.isSupported()) {
            try {
                final SystemTray tray = SystemTray.getSystemTray();
                final Dimension traySize = tray.getTrayIconSize();
                final int trayWidth = traySize.width;
                final int trayHeight = traySize.height;
                final Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
                final PopupMenu popupMenu = new PopupMenu();
                final MenuItem showLogMenuItem = new MenuItem(s("Show log..."));
                showLogMenuItem.addActionListener(e -> frame.showLog());
                popupMenu.add(showLogMenuItem);
                popupMenu.addSeparator();
                final MenuItem exitMenuItem = new MenuItem(s("Exit"));
                exitMenuItem.addActionListener(e -> frame.exitWithConfirm());
                popupMenu.add(exitMenuItem);
                popupMenu.addSeparator();
                menuRoot.fillPopupMenu(popupMenu);
                final TrayIcon icon = new TrayIcon(image, s("Marid IDE"), popupMenu);
                icon.addActionListener(ev -> frame.setVisible(!frame.isVisible()));
                frame = new IdeFrame(true, menuRoot);
                frame.setVisible(true);
                tray.add(icon);
                TrayIconHandler.addSystemHandler(icon, Level.parse(SYSPREFS.get("trayLevel", Level.OFF.getName())));
            } catch (Exception x) {
                warning(LOG, "Unable to create the tray icon", x);
                frame = new IdeFrame(false, menuRoot);
                frame.setVisible(true);
            }
        }
    }

    public static Path getProfilesDir() {
        try {
            final Path defaultDir = Paths.get(System.getProperty("user.home"), "marid", "profiles");
            final Path path = Paths.get(SYSPREFS.get("profilesDir", defaultDir.toString()));
            if (!Files.isDirectory(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception x) {
            warning(LOG, "Unable to get profiles directory", x);
            return Paths.get(System.getProperty("user.dir"));
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
