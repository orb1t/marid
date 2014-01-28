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
import org.marid.logging.Logged;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.awt.BorderLayout.NORTH;
import static java.util.Arrays.copyOfRange;
import static javax.swing.SwingConstants.HORIZONTAL;
import static org.marid.l10n.L10n.s;
import static org.marid.methods.GuiMethods.*;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMultiFrame extends AbstractFrame implements Logged {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final JPanel centerPanel = new JPanel(new BorderLayout());
    protected final JToolBar toolBar;
    protected final MultiFrameDesktop desktop;
    protected final JMenuBar menuBar;

    public AbstractMultiFrame(String title) {
        super(title);
        setJMenuBar(menuBar = new JMenuBar());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        centerPanel.add(toolBar = new JToolBar(pref.getInt("tOrientation", HORIZONTAL)), pref.get("tPosition", NORTH));
        centerPanel.add(desktop = new MultiFrameDesktop());
        add(centerPanel);
        toolBar.setBorderPainted(true);
        setPreferredSize(getDimension(pref, "size", new Dimension(700, 500)));
        doActions();
        menuBar.add(new JSeparator(JSeparator.VERTICAL));
        menuBar.add(widgetsMenu());
    }

    private JMenu widgetsMenu() {
        final JMenu widgetsMenu = new JMenu(s("Widgets"));
        final ActionListener widgetsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                        case "profiles":
                            break;
                    }
                } catch (Exception x) {
                    warning(logger, "{0} error", x, e.getActionCommand());
                }
            }
        };
        final JMenuItem cascadeItem = new JMenuItem(s("Cascade"), Images.getIcon("cascade16.png", 16));
        final JMenuItem tileVItem = new JMenuItem(s("Tile vertical"), Images.getIcon("tileVertical16.png", 16));
        final JMenuItem tileHItem = new JMenuItem(s("Tile horizontal"), Images.getIcon("tileHorizontal16.png", 16));
        cascadeItem.setActionCommand("cascade");
        cascadeItem.addActionListener(widgetsListener);
        tileVItem.setActionCommand("tileVertical");
        tileVItem.addActionListener(widgetsListener);
        tileHItem.setActionCommand("tileHorizontal");
        tileHItem.addActionListener(widgetsListener);
        widgetsMenu.add(cascadeItem);
        widgetsMenu.add(tileVItem);
        widgetsMenu.add(tileHItem);
        widgetsMenu.addSeparator();
        addWidgetListMenu(widgetsMenu);
        final JMenuItem profilesItem = new JMenuItem(s("Profiles..."), Images.getIcon("profiles16.png", 16));
        profilesItem.setActionCommand("profiles");
        profilesItem.addActionListener(widgetsListener);
        widgetsMenu.add(profilesItem);
        return widgetsMenu;
    }

    private void addWidgetListMenu(JMenu widgetsMenu) {
        final JMenu widgetListMenu = new JMenu(s("Widget list"));
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
                    final JMenuItem minimizeItem = new JMenuItem(s("Minimize"), Images.getIcon("minimize16.png"));
                    minimizeItem.setActionCommand("minimize");
                    minimizeItem.addActionListener(frameActionListener);
                    item.add(minimizeItem);
                    final JMenuItem maximizeItem = new JMenuItem(s("Maximize"), Images.getIcon("maximize16.png"));
                    maximizeItem.setActionCommand("maximize");
                    maximizeItem.addActionListener(frameActionListener);
                    item.add(maximizeItem);
                    item.addSeparator();
                    final JMenuItem normalizeItem = new JMenuItem(s("Normalize"), Images.getIcon("normalize16.png"));
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

    @Override
    public Logger getLogger() {
        return logger;
    }

    private void doActions() {
        final TreeSet<FrameAction.FrameActionElement> actions = new TreeSet<>();
        final TreeSet<FrameAction.FrameActionElement> toolbarActions = new TreeSet<>();
        for (final Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(FrameAction.class)) {
                final FrameAction fa = method.getAnnotation(FrameAction.class);
                if (fa.path().isEmpty()) {
                    continue;
                }
                final FrameAction.FrameActionElement e = new FrameAction.FrameActionElement(method, fa);
                actions.add(e);
                if (fa.tool()) {
                    toolbarActions.add(e);
                }
            }
        }
        final Map<String, JMenu> menus = new LinkedHashMap<>();
        for (final FrameAction.FrameActionElement action : actions) {
            if (!menus.containsKey(action.getPath())) {
                final String[] path = action.getPath().split("/");
                JMenu menu = null;
                for (int i = 0; i < menuBar.getMenuCount(); i++) {
                    final JMenu m = menuBar.getMenu(i);
                    if (m.getActionCommand().equals(path[0])) {
                        menu = m;
                        break;
                    }
                }
                if (menu == null) {
                    menuBar.add(menu = new JMenu(s(path[0])));
                    menu.setActionCommand(path[0]);
                }
                for (String[] p = copyOfRange(path, 1, path.length); p.length > 0; p = copyOfRange(p, 1, p.length)) {
                    JMenu subMenu = null;
                    for (int i = 0; i < menu.getMenuComponentCount(); i++) {
                        final JMenu m = (JMenu) menu.getMenuComponent(i);
                        if (m.getActionCommand().equals(p[0])) {
                            subMenu = m;
                            break;
                        }
                    }
                    if (subMenu == null) {
                        if (menu.getMenuComponentCount() == 0) {
                            menu.addSeparator();
                        }
                        menu.add(subMenu = new JMenu(s(p[0])));
                        subMenu.setActionCommand(p[0]);
                    }
                    menu = subMenu;
                }
                menus.put(action.getPath(), menu);
            }
        }
        final NavigableSet<FrameAction.FrameActionElement> descendingActions = actions.descendingSet();
        for (final FrameAction.FrameActionElement e : descendingActions) {
            final JMenu menu = menus.get(e.getPath());
            menu.insert(e.getAction(this), 0);
            final NavigableSet<FrameAction.FrameActionElement> next = descendingActions.tailSet(e, false);
            if (!next.isEmpty()) {
                if (next.first().getPath().equals(e.getPath()) && !next.first().getGroup().equals(e.getGroup())) {
                    menu.insertSeparator(0);
                }
            }
        }
        for (final FrameAction.FrameActionElement e : toolbarActions) {
            toolBar.add(e.getAction(this)).setFocusable(false);
            final NavigableSet<FrameAction.FrameActionElement> next = toolbarActions.tailSet(e, false);
            if (!next.isEmpty()) {
                if (!next.first().getPath().equals(e.getPath()) || !next.first().getGroup().equals(e.getGroup())) {
                    toolBar.addSeparator();
                }
            }
        }
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
                putDimension(pref, "size", getSize());
                pref.putInt("state", getState());
                pref.putInt("extState", getExtendedState());
                pref.put("tPosition", getToolbarPosition());
                pref.putInt("tOrientation", toolBar.getOrientation());
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

    public void showFrame(Class<?> type) {
        for (final JInternalFrame frame : desktop.getAllFrames()) {
            if (type == frame.getClass()) {
                frame.show();
                return;
            }
        }
        for (final Class<?> frameClass : getClass().getClasses()) {
            if (frameClass.isAnnotationPresent(FrameWidget.class)) {
                if (type == frameClass) {
                    try {
                        final InternalFrame frame = frameClass.isMemberClass()
                                ? (InternalFrame) frameClass.getConstructor(getClass()).newInstance(this)
                                : (InternalFrame) frameClass.newInstance();
                        desktop.add(frame);
                        frame.show();
                    } catch (Exception x) {
                        warning(logger, "{0} creating error", x, frameClass.getSimpleName());
                    }
                    return;
                }
            }
        }
        warning(logger, "No such frame: {0}", type.getSimpleName());
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

        }
    }

    protected class InternalFrame extends JInternalFrame {

        private final Preferences framePref;

        protected InternalFrame() {
            final FrameWidget meta = getClass().getAnnotation(FrameWidget.class);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle(meta.title().isEmpty() ? s(getClass().getSimpleName()) : s(meta.title()));
            setResizable(meta.resizable());
            setIconifiable(meta.iconifiable());
            setClosable(meta.closable());
            setMaximizable(meta.maximizable());
            framePref = pref.node(getClass().getSimpleName());
        }

        @Override
        public void show() {
            final int x, y;
            switch (framePref.get("position", getClass().getAnnotation(FrameWidget.class).position())) {
                case "ne":
                    x = desktop.getWidth() - getWidth();
                    y = 0;
                    break;
                case "sw":
                    x = 0;
                    y = desktop.getHeight() - getHeight();
                    break;
                case "se":
                    x = desktop.getWidth() - getWidth();
                    y = desktop.getHeight() - getHeight();
                    break;
                case "c":
                    x = (desktop.getWidth() - getWidth()) / 2;
                    y = (desktop.getHeight() - getHeight()) / 2;
                    break;
                case "nw":
                default:
                    x = 0;
                    y = 0;
                    break;
            }
            setLocation(x, y);
            super.show();
        }

        @Override
        public void dispose() {
            super.dispose();
        }
    }
}
