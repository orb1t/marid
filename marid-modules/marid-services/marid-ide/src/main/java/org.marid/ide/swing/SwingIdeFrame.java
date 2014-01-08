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

import groovy.lang.GroovyCodeSource;
import org.marid.groovy.GroovyRuntime;
import org.marid.ide.menu.MenuEntry;
import org.marid.image.MaridIcons;
import org.marid.swing.log.SwingHandler;
import org.marid.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static javax.swing.JOptionPane.*;
import static org.marid.l10n.Localized.M;
import static org.marid.l10n.Localized.S;
import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.GuiMethods.*;
import static org.marid.methods.PrefMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingIdeFrame extends JFrame {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    private static final Preferences PREFS = preferences("maridFrame");
    private final boolean closeable;

    public SwingIdeFrame(boolean closeable, List<MenuEntry> menuEntries) {
        super(S.l("Marid IDE"));
        this.closeable = closeable;
        setIconImages(MaridIcons.ICONS);
        setJMenuBar(new MenuBarImpl(menuEntries));
        setPreferredSize(getDimension(PREFS, "size", new Dimension(700, 500)));
        pack();
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(PREFS.getInt("state", getState()));
                setExtendedState(PREFS.getInt("extendedState", getExtendedState()));
                break;
            case WindowEvent.WINDOW_CLOSING:
                if (closeable) {
                    setVisible(false);
                } else {
                    final String message = M.l("Do you want to exit?");
                    switch (showConfirmDialog(this, message, S.l("Exit"), YES_NO_OPTION, QUESTION_MESSAGE)) {
                        case YES_OPTION:
                            exit();
                            break;
                    }
                }
                break;
        }
    }

    public void showLog() {
        final Logger rootLogger = Logger.getGlobal().getParent();
        if (rootLogger != null) {
            for (Handler handler : rootLogger.getHandlers()) {
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
                warning(LOG, "Unable to show log window", x);
            }
        }
    }

    public void exit() {
        PREFS.putInt("state", getState());
        PREFS.putInt("extendedState", getExtendedState());
        putDimension(PREFS, "size", getSize());
        try {
            final URL url = Utils.getClassLoader(getClass()).getResource("exitTrigger.groovy");
            if (url != null) {
                GroovyRuntime.SHELL.evaluate(new GroovyCodeSource(url));
            }
            System.exit(0);
        } catch (Exception x) {
            warning(LOG, "Unable to call exit trigger", x);
            System.exit(1);
        }
    }
}
