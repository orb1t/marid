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

package org.marid.bd.schema;

import org.marid.swing.AbstractFrame;
import org.marid.swing.menu.MenuActionList;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaFrame extends AbstractFrame {

    protected final BlockListWindow blockListWindow = new BlockListWindow(this);
    protected final SchemaEditor schemaEditor = new SchemaEditor(new SchemaModel());

    public SchemaFrame() {
        super("Schema");
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        add(schemaEditor);
        pack();
    }

    @Override
    protected void processComponentEvent(ComponentEvent e) {
        super.processComponentEvent(e);
        switch (e.getID()) {
            case ComponentEvent.COMPONENT_SHOWN:
                blockListWindow.setVisible(getPref("visible", true, "blockList"));
                break;
            case ComponentEvent.COMPONENT_HIDDEN:
                blockListWindow.setVisible(false);
                break;
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSING:
                putPref("visible", blockListWindow.isVisible(), "blockList");
                break;
        }
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                blockListWindow.dispose();
                break;
        }
    }

    @Override
    protected void fillActions(MenuActionList actionList) {
        actionList.add("main", "Schema");
        actionList.add(true, "main", "Show block list", "Schema")
                .setKey("control L")
                .setListener(e -> blockListWindow.setVisible(!blockListWindow.isVisible()));
    }
}
