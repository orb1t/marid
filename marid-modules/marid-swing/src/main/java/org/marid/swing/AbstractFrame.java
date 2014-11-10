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
import org.marid.pref.PrefSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.actions.ActionKeySupport;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.actions.MaridActions;
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

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractFrame extends JFrame implements PrefSupport, SysPrefSupport, MessageSupport, ActionKeySupport {

    public static final Border CENTER_PANEL_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    protected final JPanel centerPanel = new JPanel(new BorderLayout());
    protected final JToolBar toolBar = new JToolBar(getPref("orientation", HORIZONTAL, "toolbar"));

    public AbstractFrame(String title) {
        super(LS.s(title), WindowPrefs.graphicsConfiguration(title));
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
        menu.add(new JCheckBoxMenuItem(new MaridAction("Switch always-on-top mode", null, this::switchAlwaysOnTop)
                .setValue(Action.SELECTED_KEY, false)
                .setKey(getSysPref("alwaysOnTopKey", "control alt O"))));
        menu.addSeparator();
        menu.add(new MaridAction("Switch full screen mode", null, this::switchFullScreen)
                .setKey(getSysPref("fullScreenKey", "control alt F")));
        menu.addSeparator();
        menu.add(new JCheckBoxMenuItem(new MaridAction("Close", null, e -> dispose())
                .setValue(Action.SELECTED_KEY, false)
                .setKey(getSysPref("closeWindowKey", "control alt Q"))));
        return menu;
    }

    @PostConstruct
    public void init() {
        fillActions();
        final JMenuBar menuBar = new JMenuBar();
        final JToolBar toolBar = new JToolBar();
        MaridActions.fillMenu(getActionMap(), menuBar);
        MaridActions.fillToolbar(getActionMap(), toolBar);
        for (int i = menuBar.getComponentCount() - 1; i >= 0; i--) {
            getJMenuBar().add(menuBar.getComponent(i), 0);
        }
        for (int i = toolBar.getComponentCount() - 1; i >= 0; i--) {
            this.toolBar.add(toolBar.getComponent(i), 0);
        }
        pack();
        setBounds(getPref("bounds", new Rectangle(0, 0, 700, 500)));
        setVisible(true);
    }

    @PreDestroy
    public void destroy() {
        dispose();
    }

    protected void switchAlwaysOnTop(Action action, ActionEvent event) {
        if (isAlwaysOnTopSupported()) {
            setAlwaysOnTop((boolean) action.getValue(Action.SELECTED_KEY));
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
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(getPref("state", getState()));
                setExtendedState(getPref("extendedState", getExtendedState()));
                break;
            case WindowEvent.WINDOW_CLOSED:
                WindowPrefs.saveGraphicsDevice(this);
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

    protected abstract void fillActions();
}
