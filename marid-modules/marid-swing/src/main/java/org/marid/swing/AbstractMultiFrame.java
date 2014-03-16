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
import org.marid.logging.LogSupport;
import org.marid.swing.forms.FrameConfigurationDialog;
import org.marid.swing.menu.MenuAction;
import org.marid.swing.menu.MenuActionList;
import org.marid.swing.menu.MenuActionTreeElement;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.function.Consumer;

import static java.awt.BorderLayout.NORTH;
import static javax.swing.SwingConstants.HORIZONTAL;
import static org.marid.l10n.L10n.s;
import static org.marid.swing.MaridAction.MaridActionListener;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMultiFrame extends AbstractFrame implements LogSupport {

    protected final JPanel centerPanel = new JPanel(new BorderLayout());
    protected final JToolBar toolBar;
    protected final MultiFrameDesktop desktop;

    public AbstractMultiFrame(String title) {
        super(title);
        setJMenuBar(new JMenuBar());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        centerPanel.add(toolBar = new JToolBar(getPref("tOrientation", HORIZONTAL)), getPref("tPosition", NORTH));
        centerPanel.add(desktop = new MultiFrameDesktop());
        add(centerPanel);
        toolBar.setBorderPainted(true);
        getJMenuBar().add(widgetsMenu());
        getJMenuBar().add(new JSeparator(JSeparator.VERTICAL));
    }

    private JMenuItem menuItem(String title, String icon, String cmd, ActionListener actionListener) {
        final JMenuItem item = new JMenuItem(s(title), Images.getIcon(icon, 16));
        item.setActionCommand(cmd);
        item.addActionListener(actionListener);
        return item;
    }

    private JMenu widgetsMenu() {
        final JMenu widgetsMenu = new JMenu(s("Widgets"));
        final ActionListener widgetsListener = e -> {
            try {
                final JInternalFrame[] frames = desktop.getAllFrames();
                switch (e.getActionCommand()) {
                    case "cascade":
                        for (int i = 0; i < frames.length; i++) {
                            frames[i].setIcon(false);
                            frames[i].setLocation(i * 20, i * 20);
                            frames[i].setSize(frames[i].getPreferredSize());
                        }
                        break;
                    case "tileVertical":
                        for (int i = 0; i < frames.length; i++) {
                            frames[i].setIcon(false);
                            frames[i].setLocation(0, (desktop.getHeight() / frames.length) * i);
                            frames[i].setSize(desktop.getWidth(), desktop.getHeight() / frames.length);
                        }
                        break;
                    case "tileHorizontal":
                        for (int i = 0; i < frames.length; i++) {
                            frames[i].setIcon(false);
                            frames[i].setLocation((desktop.getWidth() / frames.length) * i, 0);
                            frames[i].setSize(desktop.getWidth() / frames.length, desktop.getHeight());
                        }
                        break;
                    case "configuration":
                        new FrameConfigurationDialog(this).setVisible(true);
                        break;
                    case "fullscreen":
                        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        final GraphicsDevice gd = ge.getDefaultScreenDevice();
                        if (gd.isFullScreenSupported()) {
                            setVisible(false);
                            try {
                                setUndecorated(!isUndecorated());
                                gd.setFullScreenWindow(AbstractMultiFrame.this);
                            } finally {
                                setVisible(true);
                            }
                        } else {
                            warning("Fullscreen mode is not supported on {0}", gd);
                        }
                        break;
                }
            } catch (Exception x) {
                warning("{0} error", x, e.getActionCommand());
            }
        };
        widgetsMenu.add(menuItem("Cascade", "cascade16.png", "cascade", widgetsListener));
        widgetsMenu.add(menuItem("Tile vertical", "tileVertical16.png", "tileVertical", widgetsListener));
        widgetsMenu.add(menuItem("Tile horizontal", "tileHorizontal16.png", "tileHorizontal", widgetsListener));
        widgetsMenu.addSeparator();
        widgetsMenu.add(widgetListMenu());
        widgetsMenu.addSeparator();
        widgetsMenu.add(menuItem("Configuration...", "configuration16.png", "configuration", widgetsListener));
        return widgetsMenu;
    }

    private JMenu widgetListMenu() {
        final JMenu widgetListMenu = new JMenu(s("Widget list"));
        widgetListMenu.setIcon(Images.getIcon("widgetList16.png", 16));
        widgetListMenu.setActionCommand("widgetList");
        widgetListMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    final ActionListener frameActionListener = event -> {
                        try {
                            switch (event.getActionCommand()) {
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
                    };
                    final JMenu item = new JMenu(frame.getTitle());
                    item.setIcon(frame.getFrameIcon());
                    item.addActionListener(e1 -> frame.show());
                    item.add(menuItem("Minimize", "minimize16.png", "minimize", frameActionListener));
                    item.add(menuItem("Maximize", "maximize16.png", "maximize", frameActionListener));
                    item.add(menuItem("Normalize", "normalize16.png", "normalize", frameActionListener));
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
        return widgetListMenu;
    }

    protected abstract void fillActions(MenuActionList actionList);

    protected ActionPutter action(String group, String name, String... path) {
        return new ActionPutter(group, name, path);
    }

    protected ActionPutter action(String group, String name, String icon, boolean toolbar, String... path) {
        return new ActionPutter(group, name, path).setIcon(icon).setToolbar(toolbar);
    }

    @Override
    public void pack() {
        final MenuActionList actions = new MenuActionList();
        fillActions(actions);
        final MenuActionTreeElement element = actions.createTreeElement();
        element.fillJMenuBar(getJMenuBar());
        actions.fillToolbar(toolBar);
        super.pack();
    }

    public void add(JInternalFrame frame) {
        desktop.add(frame);
        frame.show();
    }

    private String getToolbarPosition() {
        final String position = (String) ((BorderLayout) centerPanel.getLayout()).getConstraints(toolBar);
        return position == null ? BorderLayout.NORTH : position;
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    frame.doDefaultCloseAction();
                }
                putPref("tPosition", getToolbarPosition());
                putPref("tOrientation", toolBar.getOrientation());
                break;
            case WindowEvent.WINDOW_OPENED:
                break;
        }
    }

    public void showFrame(Class<? extends InternalFrame> type) {
        for (final JInternalFrame frame : desktop.getAllFrames()) {
            if (type == frame.getClass()) {
                frame.show();
                return;
            }
        }
        for (final Class<?> frameClass : getClass().getClasses()) {
            if (type == frameClass) {
                try {
                    final InternalFrame frame = frameClass.isMemberClass()
                            ? type.getConstructor(getClass()).newInstance(this)
                            : type.newInstance();
                    desktop.add(frame);
                    frame.show();
                } catch (Exception x) {
                    warning("{0} creating error", x, frameClass.getSimpleName());
                }
                return;
            }
        }
        warning("No such frame: {0}", type.getSimpleName());
    }

    protected class InternalFrame extends AbstractInternalFrame<AbstractMultiFrame> {

        protected InternalFrame(String name, String title, boolean closable) {
            super(AbstractMultiFrame.this, name, title, closable);
        }
    }

    protected class ActionPutter {

        private final String name;
        private final String[] path;
        private final String group;
        private MaridActionListener actionListener;
        private String icon;
        private String shortDescription;
        private String longDescription;
        private String key;
        private Consumer<Action> actionInitializer;
        private Boolean toolbar;

        private ActionPutter(String group, String name, String... path) {
            this.group = group;
            this.name = name;
            this.path = path;
        }

        public ActionPutter setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        public ActionPutter setListener(MaridActionListener listener) {
            this.actionListener = listener;
            return this;
        }

        public ActionPutter setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
            return this;
        }

        public ActionPutter setLongDescription(String longDescription) {
            this.longDescription = longDescription;
            return this;
        }

        public ActionPutter setKey(String key) {
            this.key = key;
            return this;
        }

        public ActionPutter setInitializer(Consumer<Action> actionInitializer) {
            this.actionInitializer = actionInitializer;
            return this;
        }

        public ActionPutter setToolbar(boolean toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        public Action put(MenuActionList actionList) {
            final Action action;
            if (actionListener == null) {
                action = null;
            } else {
                action = new MaridAction(name, icon, actionListener,
                        Action.SHORT_DESCRIPTION, shortDescription,
                        Action.LONG_DESCRIPTION, longDescription,
                        Action.ACCELERATOR_KEY, key == null ? null : KeyStroke.getKeyStroke(key));
            }
            final MenuAction menuAction = new MenuAction(name, group, path, action);
            menuAction.properties.put("toolbar", toolbar);
            actionList.add(menuAction);
            if (actionInitializer != null) {
                actionInitializer.accept(action);
            }
            return action;
        }
    }
}
