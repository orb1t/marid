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

package org.marid.ide.swing.servcon;

import images.Images;
import org.marid.service.MaridService;
import org.marid.service.MaridServices;
import org.marid.service.ServiceDescriptor;
import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.menu.MenuActionList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServconWindow extends AbstractMultiFrame {

    private final JList<Class<? extends MaridService>> services = new JList<>(serviceVector());
    private final JSplitPane splitPane;

    public ServconWindow() {
        super("Service configurer");
        services.setCellRenderer(new ServiceCellRenderer());
        centerPanel.remove(getDesktop());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(services), getDesktop());
        splitPane.setDividerLocation(getPref("divider", 200));
        centerPanel.add(splitPane);
        pack();
    }

    private static Vector<Class<? extends MaridService>> serviceVector() {
        final Set<Class<? extends MaridService>> set = MaridServices.serviceClasses();
        final Set<Class<? extends MaridService>> sortedSet = new TreeSet<>((c1, c2) -> {
            final ServiceDescriptor d1 = c1.getAnnotation(ServiceDescriptor.class);
            final ServiceDescriptor d2 = c2.getAnnotation(ServiceDescriptor.class);
            final String name1 = d1 == null || d1.name().isEmpty() ? c1.getSimpleName() : d1.name();
            final String name2 = d2 == null || d2.name().isEmpty() ? c2.getSimpleName() : d2.name();
            return name1.compareTo(name2);
        });
        sortedSet.addAll(set);
        return new Vector<>(sortedSet);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                putPref("divider", splitPane.getDividerLocation());
                break;
        }
    }

    @Override
    protected void fillActions(MenuActionList actionList) {
        actionList.add("mainMenu", "Configurator");
        actionList.add(true, "control", "New", "Configurator")
                .setIcon("new")
                .setKey("control N")
                .setListener((a, e) -> showFrame(() -> new ServconFrame(this)));
    }

    private static class ServiceCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> l, Object v, int index, boolean sel, boolean focus) {
            final Component c = super.getListCellRendererComponent(l, v, index, sel, focus);
            final DefaultListCellRenderer renderer = (DefaultListCellRenderer) c;
            final Class<?> serviceClass = (Class<?>) v;
            final ServiceDescriptor d = serviceClass.getAnnotation(ServiceDescriptor.class);
            final String name = s(d == null || d.name().isEmpty() ? serviceClass.getSimpleName() : d.name());
            final String icon = d == null || d.icon().isEmpty() ? "services/service.png" : d.icon();
            renderer.setText(name);
            renderer.setIcon(Images.getIcon(icon, 32));
            return renderer;
        }
    }
}
