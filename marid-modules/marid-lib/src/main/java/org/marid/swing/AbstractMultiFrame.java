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

package org.marid.swing;

import images.Images;
import org.marid.methods.LogMethods;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import static org.marid.l10n.Localized.S;
import static org.marid.methods.GuiMethods.getDimension;
import static org.marid.methods.GuiMethods.putDimension;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractMultiFrame extends AbstractFrame {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final MultiFrameToolBar toolBar;
    protected final MultiFrameDesktop desktop;
    protected final JMenuBar menuBar;

    public AbstractMultiFrame(String title) {
        super(title);
        setJMenuBar(menuBar = new JMenuBar());
        add(toolBar = new MultiFrameToolBar(), pref.get("toolbarPosition", BorderLayout.NORTH));
        add(desktop = new MultiFrameDesktop());
        toolBar.setBorderPainted(true);
        setPreferredSize(getDimension(pref, "size", new Dimension(700, 500)));
        addAction("-cascade", "Cascade", "Cascades the widgets", "control J", "Widgets");
    }

    public void cascade(ActionEvent actionEvent, Action action) {

    }

    protected void addAction(String cmd, String name, String desc, String key, String... path) {
        final boolean addToToolbar;
        if (cmd.startsWith("-")) {
            addToToolbar = false;
            cmd = cmd.substring(1);
        } else {
            addToToolbar = true;
        }
        final boolean addSeparator;
        if (cmd.endsWith("|")) {
            addSeparator = true;
            cmd = cmd.substring(0, cmd.length() - 1);
        } else {
            addSeparator = false;
        }
        final Action action = new AbstractAction(S.l(name)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String method = (String) getValue(Action.ACTION_COMMAND_KEY);
                final Object caller = AbstractMultiFrame.this;
                try {
                    caller.getClass().getMethod(method, ActionEvent.class, Action.class).invoke(caller, e, this);
                } catch (Exception x) {
                    LogMethods.warning(logger, "{0} error", x, method);
                }
            }
        };
        action.putValue(Action.ACTION_COMMAND_KEY, cmd);
        final ImageIcon icon16 = Images.getIcon(cmd + "16.png", 16);
        final ImageIcon icon24 = Images.getIcon(cmd + "24.png", 24);
        if (icon16 != null) {
            action.putValue(Action.SMALL_ICON, icon16);
        }
        if (icon24 != null) {
            action.putValue(Action.LARGE_ICON_KEY, icon24);
        }
        if (desc != null) {
            action.putValue(Action.SHORT_DESCRIPTION, S.l(desc));
        }
        if (key != null) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
        }
        if (addToToolbar) {
            toolBar.add(action);
            if (addSeparator) {
                final Component lastButton = toolBar.getComponent(toolBar.getComponentCount() - 1);
                if (!(lastButton instanceof JSeparator)) {
                    toolBar.addSeparator();
                }
            }
        }
        if (path == null || path.length == 0) {
            return;
        }
        JMenu menu = null;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            final JMenu m = menuBar.getMenu(i);
            if (path[0].equals(m.getActionCommand())) {
                menu = m;
                break;
            }
        }
        if (menu == null) {
            menuBar.add(menu = new JMenu(S.l(path[0])));
            menu.setActionCommand(path[0]);
        }
        for (int i = 1; i < path.length; i++) {
            final String p = path[i];
            JMenu m = null;
            for (int j = 0; j < menu.getItemCount(); j++) {
                final JMenuItem item = menu.getItem(j);
                if (p.equals(item.getActionCommand()) && item instanceof JMenu) {
                    m = (JMenu) item;
                    break;
                }
            }
            if (m == null) {
                menu.add(m = new JMenu(S.l(p)));
                m.setActionCommand(p);
            }
            menu = m;
        }
        menu.add(action);
        if (addSeparator) {
            menu.addSeparator();
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                final BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
                putDimension(pref, "size", getSize());
                pref.putInt("state", getState());
                pref.putInt("extState", getExtendedState());
                pref.put("toolbarPosition", (String) borderLayout.getConstraints(toolBar));
                pref.putInt("toolbarOrientation", toolBar.getOrientation());
                break;
            case WindowEvent.WINDOW_OPENED:
                setState(pref.getInt("state", getState()));
                setExtendedState(pref.getInt("extState", getExtendedState()));
                break;
        }
    }

    public void addFrame(JInternalFrame frame) {
        desktop.getDesktopManager().openFrame(frame);
    }

    protected class MultiFrameToolBar extends JToolBar {

        protected MultiFrameToolBar() {
            super(pref.getInt("toolbarOrientation", JToolBar.HORIZONTAL));
        }

        @Override
        public JButton add(Action a) {
            final JButton button = super.add(a);
            button.setFocusable(false);
            return button;
        }
    }

    protected class MultiFrameDesktop extends JDesktopPane {

        protected MultiFrameDesktop() {
            setDesktopManager(new MultiFrameDesktopManager());
        }

        @Override
        public MultiFrameDesktopManager getDesktopManager() {
            return (MultiFrameDesktopManager) super.getDesktopManager();
        }

        protected class MultiFrameDesktopManager extends DefaultDesktopManager {

            @Override
            public void openFrame(JInternalFrame f) {
                super.openFrame(f);
                for (final JInternalFrame frame : getAllFrames()) {
                    if (frame == f) {
                        return;
                    }
                }
                MultiFrameDesktop.this.add(f);
                f.setVisible(true);
            }
        }
    }
}
