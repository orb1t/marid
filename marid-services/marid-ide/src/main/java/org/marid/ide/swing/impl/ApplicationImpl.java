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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.marid.methods.PrefMethods.preferences;
import static org.marid.methods.LogMethods.warning;

public class ApplicationImpl implements Application {

    private final Logger log = Logger.getLogger(ApplicationImpl.class.getName());
    private final FrameImpl frame;

    public ApplicationImpl() {
        final BlockingQueue<List<MenuEntry>> menuEntries = new SynchronousQueue<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MenuEntry> entries = new ArrayList<>();
                for (MaridMenu menu : ServiceLoader.load(MaridMenu.class)) {
                    entries.addAll(menu.getMenuEntries());
                }
                try {
                    menuEntries.put(entries);
                } catch (Exception x) {
                    warning(log, "Menu entries error", x);
                }
            }
        }).start();
        String laf = preferences("laf").get("laf", NimbusLookAndFeel.class.getName());
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception x) {
            warning(log, "{0} error", x, laf);
        }
        final BlockingQueue<FrameImpl> frameQ = new SynchronousQueue<>();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<MenuEntry> entries = null;
                try {
                    entries = menuEntries.take();
                    FrameImpl frame = new FrameImpl(ApplicationImpl.this, entries);
                    frame.setVisible(true);
                    frameQ.put(frame);
                } catch (Exception x) {
                    warning(log, "Init frame error", x);
                }
                if (entries != null) {
                    try {
                        initTray(entries);
                    } catch (Exception x) {
                        warning(log, "Tray initialization error", x);
                    }
                }
            }
        });
        try {
            frame = frameQ.poll(3, TimeUnit.MINUTES);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
        if (frame == null) {
            warning(log, "Frame is null");
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
        String dir = SYSPREF.node("common").get("libDir", null);
        if (dir == null) {
            dir = Paths.get(System.getProperty("user.home"), "marid", "lib").toString();
        }
        return dir;
    }

    @Override
    public String getTempDirectory() {
        String dir = SYSPREF.node("common").get("tempDir", null);
        if (dir == null) {
            dir = Paths.get(System.getProperty("user.home"), "marid", "temp").toString();
        }
        return dir;
    }

    @Override
    public String getOutputDirectory() {
        String dir = SYSPREF.node("common").get("outDir", null);
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
        Logger rootLogger = Logger.getGlobal().getParent();
        if (rootLogger != null) {
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof SwingHandler) {
                    ((SwingHandler)handler).show();
                    return;
                }
            }
            try {
                SwingHandler swingHandler = new SwingHandler();
                rootLogger.addHandler(swingHandler);
                swingHandler.show();
            } catch (Exception x) {
                warning(log, "Unable to create SWING log handler", x);
            }
        }
    }

    private void initTray(List<MenuEntry> entries) throws Exception {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Dimension traySize = tray.getTrayIconSize();
            int trayWidth = traySize.width;
            int trayHeight = traySize.height;
            Image image = MaridIcon.getImage(Math.min(trayWidth, trayHeight), Color.GREEN);
            PopupMenuImpl popup = new PopupMenuImpl(this, entries);
            TrayIcon icon = new TrayIcon(image, S.l("Marid IDE"), popup);
            icon.addActionListener(popup);
            icon.setActionCommand("show_hide");
            tray.add(icon);
        }
    }
}
