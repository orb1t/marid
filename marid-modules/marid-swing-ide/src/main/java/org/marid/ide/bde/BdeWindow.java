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

package org.marid.ide.bde;

import org.marid.bde.view.BlockEditor;
import org.marid.swing.AbstractFrame;
import org.marid.swing.menu.MenuActionList;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class BdeWindow extends AbstractFrame implements BdeConfiguration {

    private final ServconServices services = new ServconServices();
    private final BlockEditor blockEditor = new BlockEditor();
    private final JSplitPane splitPane;

    public BdeWindow() {
        super("Service configurer");
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(services), blockEditor);
        splitPane.setDividerLocation(getPref("divider", 200));
        centerPanel.add(splitPane);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                blockEditor.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                blockEditor.stop();
            }
        });
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
    }
}
