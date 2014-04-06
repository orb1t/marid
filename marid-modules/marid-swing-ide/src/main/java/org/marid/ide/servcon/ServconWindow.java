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

package org.marid.ide.servcon;

import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.menu.MenuActionList;

import javax.swing.*;
import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServconWindow extends AbstractMultiFrame {

    private final ServconServices services = new ServconServices();
    private final JSplitPane splitPane;

    public ServconWindow() {
        super("Service configurer");
        centerPanel.remove(getDesktop());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(services), getDesktop());
        splitPane.setDividerLocation(getPref("divider", 200));
        centerPanel.add(splitPane);
        pack();
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
}
