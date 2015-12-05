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

package org.marid.ide.gui;

import org.marid.dyn.MetaInfo;
import org.marid.ide.base.Ide;
import org.marid.ide.base.IdeFrame;
import org.marid.ide.widgets.Widget;
import org.marid.image.MaridIcons;
import org.marid.logging.LogSupport;
import org.marid.logging.Logging;
import org.marid.pref.PrefSupport;
import org.marid.swing.WindowPrefs;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.actions.MaridActions;
import org.marid.swing.log.SwingHandler;
import org.marid.swing.menu.SwingMenuBarWrapper;
import org.marid.swing.util.MessageSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.logging.Handler;

import static javax.swing.JOptionPane.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("ideFrame")
public class IdeFrameImpl extends JFrame implements IdeFrame, PrefSupport, LogSupport, MessageSupport {

    private final IdeImpl ide;

    @Autowired
    private IdeDesktopImpl desktop;

    @Autowired
    private IdeStatusLineImpl statusLine;

    @Autowired
    public IdeFrameImpl(IdeImpl ide, ActionMap ideActionMap) {
        super(LS.s("Marid IDE"), WindowPrefs.graphicsConfiguration("IDE"));
        setName("IDE");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.ide = ide;
        setIconImages(MaridIcons.ICONS);
        setJMenuBar(new JMenuBar());
        getRootPane().setActionMap(ideActionMap);
    }

    @PostConstruct
    private void init() {
        add(desktop);
        add(statusLine, BorderLayout.SOUTH);
        pack();
        setBounds(getPref("bounds", new Rectangle(0, 0, 700, 500)));
        setState(getPref("state", getState()));
        setExtendedState(getPref("extendedState", getExtendedState()));
        setVisible(true);
    }

    @PreDestroy
    private void destroy() {
        dispose();
    }

    @Override
    public Ide getIde() {
        return ide;
    }

    @Autowired
    public void setWidgetsMenu(IdeDesktopImpl ideDesktop, ConfigurableApplicationContext context) {
        final Map<String, Set<Action>> actions = new TreeMap<>();
        for (final String beanName : context.getBeanNamesForType(Widget.class)) {
            final MetaInfo metaInfo = context.findAnnotationOnBean(beanName, MetaInfo.class);
            final Comparator<Action> actionComparator = Comparator.comparing(a -> (String) a.getValue(Action.NAME));
            actions.computeIfAbsent(metaInfo.group(), g -> new TreeSet<>(actionComparator)).add(
                    new MaridAction(metaInfo.name(), metaInfo.icon(), ev -> {
                        final Widget widget = context.getBean(beanName, Widget.class);
                        ideDesktop.add(widget);
                        widget.show();
                    }));
        }
        if (!actions.isEmpty()) {
            final JMenu menu = new JMenu(s("Widgets"));
            actions.values().forEach(as -> {
                if (menu.getMenuComponentCount() > 0) {
                    menu.addSeparator();
                }
                as.forEach(menu::add);
            });
            getJMenuBar().add(menu);
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSING:
                WindowPrefs.saveGraphicsDevice(this);
                putPref("state", getState());
                putPref("extendedState", getExtendedState());
                if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    putPref("bounds", getBounds());
                }
                break;
            case WindowEvent.WINDOW_OPENED:
                MaridActions.fillMenu(getRootPane(), new SwingMenuBarWrapper(getJMenuBar()));
                setState(getPref("state", getState()));
                setExtendedState(getPref("extendedState", getExtendedState()));
                break;
        }
        super.processWindowEvent(e);
    }

    @Override
    public IdeStatusLineImpl getStatusLine() {
        return statusLine;
    }

    @Override
    public IdeDesktopImpl getDesktop() {
        return desktop;
    }

    public void showLog() {
        for (final Handler handler : Logging.rootLogger().getHandlers()) {
            if (handler instanceof SwingHandler) {
                ((SwingHandler) handler).show();
            }
        }
    }

    public void exitWithConfirm() {
        switch (showConfirmDialog(null, m("Do you want to exit?"), s("Exit"), YES_NO_OPTION, QUESTION_MESSAGE)) {
            case YES_OPTION:
                ide.exit();
                break;
        }
    }
}
