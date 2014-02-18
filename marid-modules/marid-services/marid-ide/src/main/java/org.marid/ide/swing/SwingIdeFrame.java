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

import org.marid.ide.menu.MenuEntry;
import org.marid.image.MaridIcons;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.log.SwingHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.*;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingIdeFrame extends JFrame implements PrefSupport, LogSupport {

    private final boolean closeable;

    public SwingIdeFrame(boolean closeable, List<MenuEntry> menuEntries) {
        super(s("Marid IDE"));
        this.closeable = closeable;
        setIconImages(MaridIcons.ICONS);
        setJMenuBar(new MenuBarImpl(menuEntries));
        setLocationByPlatform(true);
        pack();
        setSize(getPref("size", new Dimension(700, 500)));
        setLocation(getPref("location", getLocation()));
        setState(getPref("state", getState()));
        setExtendedState(getPref("extendedState", getExtendedState()));
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(getPref("state", getState()));
                setExtendedState(getPref("extendedState", getExtendedState()));
                break;
            case WindowEvent.WINDOW_CLOSING:
                if (closeable) {
                    setVisible(false);
                } else {
                    exitWithConfirm();
                }
                break;
        }
    }

    public void showLog() {
        final Logger rootLogger = Logger.getGlobal().getParent();
        if (rootLogger != null) {
            for (final Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof SwingHandler) {
                    ((SwingHandler) handler).show();
                    return;
                }
            }
            try {
                final SwingHandler swingHandler = new SwingHandler();
                rootLogger.addHandler(swingHandler);
                swingHandler.show();
            } catch (Exception x) {
                warning("Unable to show log window", x);
            }
        }
    }

    public void exitWithConfirm() {
        switch (showConfirmDialog(null, m("Do you want to exit?"), s("Exit"), YES_NO_OPTION, QUESTION_MESSAGE)) {
            case YES_OPTION:
                exit();
                break;
        }
    }

    public void exit() {
        putPref("state", getState());
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
            putPref("size", getSize());
            putPref("location", getLocation());
        }
        putPref("extendedState", getExtendedState());
        dispose();
        System.exit(0);
    }
}
