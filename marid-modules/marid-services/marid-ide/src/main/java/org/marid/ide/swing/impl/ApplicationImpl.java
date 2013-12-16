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

package org.marid.ide.swing.impl;

import org.marid.ide.itf.Application;
import org.marid.ide.menu.MaridMenu;
import org.marid.ide.menu.MenuEntry;
import org.marid.ide.swing.impl.dialogs.OutputBuilderDialogImpl;
import org.marid.ide.swing.impl.dialogs.PreferencesDialogImpl;
import org.marid.image.MaridIcon;
import org.marid.swing.log.SwingHandler;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.marid.ide.swing.impl.MaridSwingPrefs.SYS_PREFS;
import static org.marid.methods.LogMethods.warning;

public class ApplicationImpl implements Application {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    private final FrameImpl frame;

    public ApplicationImpl() {
        final List<MenuEntry> menuEntries = getMenuEntries();
        installLaf();
        frame = new FrameImpl(ApplicationImpl.this, menuEntries);
        initTray(menuEntries);
    }

    private List<MenuEntry> getMenuEntries() {
        final List<MenuEntry> entries = new LinkedList<>();
        try {
            for (final MaridMenu menu : ServiceLoader.load(MaridMenu.class)) {
                entries.addAll(menu.getMenuEntries());
            }
        } catch (Exception x) {
            warning(LOG, "Unable to load menu entries", x);
        }
        return entries;
    }

    private void installLaf() {
        final String laf = SYS_PREFS.get("laf", NimbusLookAndFeel.class.getName());
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception x) {
            warning(LOG, "{0} error", x, laf);
        }
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public FrameImpl getFrame() {
        return frame;
    }

    @Override
    public String getLibDirectory() {
        String dir = SYS_PREFS.node("common").get("libDir", null);
        if (dir == null) {
            dir = Paths.get(System.getProperty("user.home"), "marid", "lib").toString();
        }
        return dir;
    }

    @Override
    public String getTempDirectory() {
        String dir = SYS_PREFS.node("common").get("tempDir", null);
        if (dir == null) {
            dir = Paths.get(System.getProperty("user.home"), "marid", "temp").toString();
        }
        return dir;
    }

    @Override
    public String getOutputDirectory() {
        String dir = SYS_PREFS.node("common").get("outDir", null);
        if (dir == null) {
            dir = Paths.get(System.getProperty("user.home"), "marid", "out").toString();
        }
        return dir;
    }

    @Override
    public void showPreferencesDialog() {
        new PreferencesDialogImpl(frame).setVisible(true);
    }

    @Override
    public void showOutputBuilder() {
        new OutputBuilderDialogImpl(frame).setVisible(true);
    }

    @Override
    public void showLog() {
        final Logger rootLogger = Logger.getGlobal().getParent();
        if (rootLogger != null) {
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof SwingHandler) {
                    ((SwingHandler)handler).show();
                    return;
                }
            }
            try {
                final SwingHandler swingHandler = new SwingHandler();
                rootLogger.addHandler(swingHandler);
                swingHandler.show();
            } catch (Exception x) {
                warning(LOG, "Unable to create SWING log handler", x);
            }
        }
    }

    private void initTray(List<MenuEntry> entries) {
        if (SystemTray.isSupported()) {
            try {
                final SystemTray tray = SystemTray.getSystemTray();
                final Dimension traySize = tray.getTrayIconSize();
                final int trayWidth = traySize.width;
                final int trayHeight = traySize.height;
                final Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
                final PopupMenuImpl popup = new PopupMenuImpl(this, entries);
                final TrayIcon icon = new TrayIcon(image, S.l("Marid IDE"), popup);
                icon.addActionListener(popup);
                icon.setActionCommand("show_hide");
                tray.add(icon);
            } catch (Exception x) {
                warning(LOG, "Unable to create the tray icon", x);
            }
        }
    }
}
