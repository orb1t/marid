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

import org.marid.image.MaridIcons;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.menu.ActionTreeElement;
import org.marid.swing.menu.MenuActionList;
import org.marid.swing.util.MessageSupport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import static java.awt.BorderLayout.NORTH;
import static javax.swing.SwingConstants.HORIZONTAL;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractFrame extends JFrame implements PrefSupport, SysPrefSupport, LogSupport, MessageSupport {

    public static final Border CENTER_PANEL_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    protected final JPanel centerPanel = new JPanel(new BorderLayout());
    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));

    public AbstractFrame(String title) {
        super(s(title));
        setName(title);
        setJMenuBar(new JMenuBar());
        setUndecorated(getPref("undecorated", getSysPref("undecorated", false, "windows")));
        centerPanel.setBorder(CENTER_PANEL_BORDER);
        centerPanel.add(toolBar, getPref("pos", NORTH, "toolbar"));
        toolBar.setOpaque(true);
        toolBar.setBorderPainted(true);
        toolBar.setVisible(getPref("visible", true, "toolbar"));
        add(centerPanel);
        setIconImages(MaridIcons.ICONS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setState(getPref("state", getState()));
        setExtendedState(getPref("extendedState", getExtendedState()));
        getJMenuBar().add(windowMenu());
    }

    private JMenu windowMenu() {
        final JMenu menu = new JMenu(s("Window"));
        menu.add(new MaridAction("Switch always-on-top mode", null, this::switchAlwaysOnTop)
                .setKey(getSysPref("alwaysOnTopKey", "control alt O")));
        menu.addSeparator();
        menu.add(new MaridAction("Switch full screen mode", null, this::switchFullScreen)
                .setKey(getSysPref("fullScreenKey", "control alt F")));
        menu.addSeparator();
        menu.add(new MaridAction("Close", null, e -> dispose())
                .setKey(getSysPref("closeWindowKey", "control alt Q")));
        return menu;
    }

    @PostConstruct
    public void init() {
        setVisible(true);
    }

    @PreDestroy
    public void destroy() {
        dispose();
    }

    protected void switchAlwaysOnTop(ActionEvent event) {
        if (isAlwaysOnTopSupported()) {
            setAlwaysOnTop(!isAlwaysOnTop());
        } else {
            showMessage(WARNING_MESSAGE, "Warning", "Always on top windows are not supported");
        }
    }

    protected void switchFullScreen(ActionEvent event) {
        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice device = environment.getDefaultScreenDevice();
        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(device.getFullScreenWindow() == this ? null : this);
        } else {
            showMessage(WARNING_MESSAGE, "Warning", "Full-screen mode is not supported");
        }
    }

    @Override
    public void pack() {
        getJMenuBar().add(new JSeparator(JSeparator.VERTICAL));
        final MenuActionList actions = new MenuActionList();
        fillActions(actions);
        final ActionTreeElement element = new ActionTreeElement(actions);
        element.fillJMenuBar(getJMenuBar());
        actions.fillToolbar(toolBar);
        super.pack();
        setBounds(getPref("bounds", new Rectangle(0, 0, 700, 500)));
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(getPref("state", getState()));
                setExtendedState(getPref("extendedState", getExtendedState()));
                break;
            case WindowEvent.WINDOW_CLOSED:
                putPref("pos", getToolbarPosition(), "toolbar");
                putPref("orientation", toolBar.getOrientation(), "toolbar");
                putPref("visible", toolBar.isVisible());
                if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    putPref("bounds", getBounds());
                }
                putPref("state", getState());
                putPref("extendedState", getExtendedState());
                break;
        }
    }

    private String getToolbarPosition() {
        final String position = (String) ((BorderLayout) centerPanel.getLayout()).getConstraints(toolBar);
        return position == null ? BorderLayout.NORTH : position;
    }

    protected abstract void fillActions(MenuActionList actionList);
}
