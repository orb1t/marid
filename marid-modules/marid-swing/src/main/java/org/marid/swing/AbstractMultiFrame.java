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

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMultiFrame extends AbstractFrame {

    private final MultiFrameDesktop desktop;

    public AbstractMultiFrame(String title) {
        super(title);
        centerPanel.add(desktop = new MultiFrameDesktop());
        getJMenuBar().add(widgetsMenu());
    }

    public MultiFrameDesktop getDesktop() {
        return desktop;
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
                            log(WARNING, "Fullscreen mode is not supported on {0}", gd);
                        }
                        break;
                }
            } catch (Exception x) {
                log(WARNING, "{0} error", x, e.getActionCommand());
            }
        };
        widgetsMenu.add(menuItem("Cascade", "cascade16.png", "cascade", widgetsListener));
        widgetsMenu.add(menuItem("Tile vertical", "tileVertical16.png", "tileVertical", widgetsListener));
        widgetsMenu.add(menuItem("Tile horizontal", "tileHorizontal16.png", "tileHorizontal", widgetsListener));
        widgetsMenu.addSeparator();
        widgetsMenu.add(widgetListMenu());
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

    public void add(JInternalFrame frame) {
        desktop.add(frame);
        frame.show();
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    frame.doDefaultCloseAction();
                }
                break;
            case WindowEvent.WINDOW_OPENED:
                break;
        }
    }

    protected <F extends SingletonInternalFrame> void showSingletonFrame(Supplier<F> frameSupplier) {
        Class<?> type = null;
        for (final java.lang.reflect.Type t : frameSupplier.getClass().getGenericInterfaces()) {
            if (t instanceof ParameterizedType && t.getTypeName().equals(Supplier.class.getName())) {
                final ParameterizedType pt = (ParameterizedType) t;
                if (pt.getActualTypeArguments().length == 1 && pt.getActualTypeArguments()[0] instanceof Class<?>) {
                    type = (Class<?>) pt.getActualTypeArguments()[0];
                    break;
                }
            }
        }
        if (type == null) {
            type = frameSupplier.get().getClass();
        }
        for (final JInternalFrame frame : desktop.getAllFrames()) {
            if (frame.getClass() == type) {
                frame.show();
                return;
            }
        }
        createAndShowFrame(frameSupplier);
    }

    protected <F extends AbstractInternalFrame> void showFrame(Supplier<F> frameSupplier) {
        createAndShowFrame(frameSupplier);
    }

    protected void createAndShowFrame(Supplier<? extends JInternalFrame> frameSupplier) {
        final JInternalFrame frame = frameSupplier.get();
        desktop.add(frame);
        frame.show();
    }

    protected class IntFrame extends AbstractInternalFrame<AbstractMultiFrame> {

        protected IntFrame(String title) {
            super(AbstractMultiFrame.this, title);
        }
    }

    protected class SingletonIntFrame extends SingletonInternalFrame<AbstractMultiFrame> {

        protected SingletonIntFrame(String title) {
            super(AbstractMultiFrame.this, title);
        }
    }
}
