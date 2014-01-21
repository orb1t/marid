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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.awt.BorderLayout.NORTH;
import static javax.swing.SwingConstants.HORIZONTAL;
import static org.marid.l10n.Localized.S;
import static org.marid.methods.GuiMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractMultiFrame extends AbstractFrame {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final JToolBar toolBar;
    protected final MultiFrameDesktop desktop;
    protected final JMenuBar menuBar;

    public AbstractMultiFrame(String title) {
        super(title);
        setJMenuBar(menuBar = new JMenuBar());
        add(toolBar = new JToolBar(pref.getInt("toolbarOrientation", HORIZONTAL)), pref.get("toolbarPosition", NORTH));
        add(desktop = new MultiFrameDesktop());
        toolBar.setBorderPainted(true);
        setPreferredSize(getDimension(pref, "size", new Dimension(700, 500)));
        addAction("-cascade", "Cascade", "Cascades the widgets", "control J", "Widgets");
        addAction("-tileHorizontal", "Tile horizontal", "Tiles the widgets horizontally", "control H", "Widgets");
        addAction("-tileVertical|", "Tile vertical", "Tiles the widgets vertically", "control K", "Widgets");
        addWidgetListMenu();
        addAction("-profiles", "Profiles...", "Shows the profiles dialog", null, "Widgets");
    }

    public void cascade(ActionEvent actionEvent, Action action) throws PropertyVetoException {
        final JInternalFrame[] frames = desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            frames[i].setIcon(false);
            frames[i].setLocation(i * 20, i * 20);
            frames[i].setSize(frames[i].getPreferredSize());
        }
    }

    public void tileVertical(ActionEvent actionEvent, Action action) throws PropertyVetoException {
        final JInternalFrame[] frames = desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            frames[i].setIcon(false);
            frames[i].setLocation(0, (desktop.getHeight() / frames.length) * i);
            frames[i].setSize(desktop.getWidth(), desktop.getHeight() / frames.length);
        }
    }

    public void tileHorizontal(ActionEvent actionEvent, Action action) throws PropertyVetoException {
        final JInternalFrame[] frames = desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            frames[i].setIcon(false);
            frames[i].setLocation((desktop.getWidth() / frames.length) * i, 0);
            frames[i].setSize(desktop.getWidth() / frames.length, desktop.getHeight());
        }
    }

    public void profiles(ActionEvent actionEvent, Action action) {

    }

    private void addWidgetListMenu() {
        final JMenu widgetsMenu = menuBar.getMenu(0);
        final JMenu widgetListMenu = new JMenu(S.l("Widget list"));
        widgetListMenu.setIcon(Images.getIcon("widgetList16.png", 16));
        widgetListMenu.setActionCommand("widgetList");
        widgetListMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    final ActionListener frameActionListener = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                switch (e.getActionCommand()) {
                                    case "maximize":
                                        frame.setMaximum(true);
                                        break;
                                    case "minimize":
                                        frame.setIcon(true);
                                        break;
                                    case "normalize":
                                        frame.setIcon(false);
                                        frame.setMaximum(false);
                                        frame.setLocation(0, 0);
                                        frame.setSize(frame.getPreferredSize());
                                        break;
                                }
                            } catch (PropertyVetoException x) {
                                // skip
                            }
                        }
                    };
                    final JMenu item = new JMenu(frame.getTitle());
                    item.setIcon(frame.getFrameIcon());
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                frame.setIcon(false);
                                frame.setSelected(true);
                            } catch (PropertyVetoException x) {
                                // skip
                            }
                        }
                    });
                    final JMenuItem minimizeItem = new JMenuItem(S.l("Minimize"), Images.getIcon("minimize16.png"));
                    minimizeItem.setActionCommand("minimize");
                    minimizeItem.addActionListener(frameActionListener);
                    item.add(minimizeItem);
                    final JMenuItem maximizeItem = new JMenuItem(S.l("Maximize"), Images.getIcon("maximize16.png"));
                    maximizeItem.setActionCommand("maximize");
                    maximizeItem.addActionListener(frameActionListener);
                    item.add(maximizeItem);
                    item.addSeparator();
                    final JMenuItem normalizeItem = new JMenuItem(S.l("Normalize"), Images.getIcon("normalize16.png"));
                    normalizeItem.setActionCommand("normalize");
                    normalizeItem.addActionListener(frameActionListener);
                    item.add(normalizeItem);
                    widgetListMenu.add(item);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                widgetListMenu.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                widgetListMenu.removeAll();
            }
        });
        widgetsMenu.add(widgetListMenu);
        widgetsMenu.addSeparator();
    }

    public void widgetList(ActionEvent actionEvent, Action action) throws PropertyVetoException {

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
                } catch (NoSuchMethodException x) {
                    LogMethods.warning(logger, "{0} No command registered", x, method);
                } catch (Exception x) {
                    LogMethods.severe(logger, "{0} error", x, method);
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
            toolBar.add(action).setFocusable(false);
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
            if (m != null && path[0].equals(m.getActionCommand())) {
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
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    final Preferences framePref = pref.node(frame.getName());
                    putPoint(framePref, "location", frame.getLocation());
                }
                break;
            case WindowEvent.WINDOW_OPENED:
                setState(pref.getInt("state", getState()));
                setExtendedState(pref.getInt("extState", getExtendedState()));
                break;
        }
    }

    public void addFrame(JInternalFrame frame) {
        final Preferences framePref = pref.node(frame.getName());
        frame.setLocation(getPoint(framePref, "location", new Point(0, 0)));
        desktop.getDesktopManager().openFrame(frame);
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
