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

package org.marid.ide;

import org.marid.dyn.MetaInfo;
import org.marid.ide.widgets.Widget;
import org.marid.ide.widgets.WidgetProviders;
import org.marid.image.MaridIcons;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.forms.ConfigurationProvider;
import org.marid.swing.forms.Form;
import org.marid.swing.forms.StaticConfigurationDialog;
import org.marid.swing.log.SwingHandler;
import org.marid.swing.menu.MenuActionTreeElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.*;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeFrame extends JFrame implements PrefSupport, LogSupport {

    private final boolean closeable;
    private final IdeDesktop desktop;
    private final IdeStatusLine statusLine;

    public IdeFrame(boolean closeable, MenuActionTreeElement menuRoot) {
        super(s("Marid IDE"));
        this.closeable = closeable;
        setIconImages(MaridIcons.ICONS);
        setJMenuBar(new JMenuBar());
        setLocationByPlatform(true);
        getJMenuBar().add(widgetsMenu());
        getJMenuBar().add(preferencesMenu());
        getJMenuBar().add(new JSeparator(JSeparator.VERTICAL));
        menuRoot.fillJMenuBar(getJMenuBar());
        add(desktop = new IdeDesktop());
        add(statusLine = new IdeStatusLine(this), BorderLayout.SOUTH);
        pack();
        setBounds(getPref("bounds", new Rectangle(0, 0, 700, 500)));
        setState(getPref("state", getState()));
        setExtendedState(getPref("extendedState", getExtendedState()));
    }

    private JMenu widgetsMenu() {
        final JMenu menu = new JMenu(s("Widgets"));
        for (final Class<? extends Widget> widgetType : WidgetProviders.widgetProviders()) {
            final MetaInfo metaInfo = widgetType.getAnnotation(MetaInfo.class);
            menu.add(new MaridAction(metaInfo.name(), metaInfo.icon(), e -> {
                for (final JInternalFrame frame : desktop.getAllFrames()) {
                    if (widgetType == frame.getClass()) {
                        frame.show();
                        return;
                    }
                }
                try {
                    final Constructor<? extends Widget> c = widgetType.getConstructor(IdeFrame.class);
                    final Widget widget = c.newInstance(this);
                    desktop.add(widget);
                    widget.show();
                } catch (Exception x) {
                    warning("Unable to create widget: {0}", x, widgetType);
                }
            }));
        }
        return menu;
    }

    private JMenu preferencesMenu() {
        final JMenu menu = new JMenu(s("Preferences"));
        ConfigurationProvider.visitConfigurations(c -> {
            final Form form = c.getAnnotation(Form.class);
            final String icon = form == null || form.icon().isEmpty() ? null : form.icon();
            final String description = form == null || form.description().isEmpty() ? null : s(form.description());
            menu.add(new MaridAction(
                    StaticConfigurationDialog.nameFor(c),
                    icon,
                    e -> new StaticConfigurationDialog(IdeFrame.getIdeFrame(), c).setVisible(true),
                    Action.SHORT_DESCRIPTION, description));
        });
        return menu;
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
        for (final Handler handler : Logger.getGlobal().getParent().getHandlers()) {
            if (handler instanceof SwingHandler) {
                ((SwingHandler) handler).show();
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
        putPref("extendedState", getExtendedState());
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
            putPref("bounds", getBounds());
        }
        dispose();
        System.exit(0);
    }

    public static IdeFrame getIdeFrame() {
        return (IdeFrame) Arrays.stream(Frame.getFrames()).filter(f -> f instanceof IdeFrame).findFirst().get();
    }
}
