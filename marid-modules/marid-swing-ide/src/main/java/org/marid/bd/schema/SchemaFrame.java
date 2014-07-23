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

import org.marid.bd.BlockComponent;
import org.marid.bd.shapes.LinkShapeEvent;
import org.marid.swing.AbstractFrame;
import org.marid.swing.actions.ComponentAction;
import org.marid.swing.menu.MenuActionList;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static javax.swing.BorderFactory.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaFrame extends AbstractFrame implements SchemaFrameConfiguration {

    protected final BlockListWindow blockListWindow = new BlockListWindow(this);
    protected final SchemaEditor schemaEditor = new SchemaEditor(this);
    protected final JLayer<SchemaEditor> layer = new JLayer<>(schemaEditor, new SchemaEditorLayerUI());

    public SchemaFrame() {
        super("Schema");
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        centerPanel.add(layer);
        getContentPane().setBackground(getBackground());
        pack();
    }

    protected void fireEvent(AWTEvent event) {
        layer.getUI().eventDispatched(event, layer);
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
            case WindowEvent.WINDOW_OPENED:
                schemaEditor.start();
                break;
            case WindowEvent.WINDOW_CLOSED:
                schemaEditor.stop();
                blockListWindow.dispose();
                break;
        }
    }

    @Override
    protected void fillActions(MenuActionList actionList) {
        actionList.add("main", "Schema");
        final Action showBlockListAction = actionList.add("main", "Show block list", "Schema")
                .setKey("control L")
                .setIcon("item")
                .setListener(e -> blockListWindow.setVisible(!blockListWindow.isVisible()));
        actionList.add(true, "zoom", "Zoom in", "Schema")
                .setKey("control I")
                .setIcon("zoomin")
                .setListener(e -> schemaEditor.zoomIn());
        actionList.add(true, "zoom", "Zoom out", "Schema")
                .setKey("control I")
                .setIcon("zoomout")
                .setListener(e -> schemaEditor.zoomOut());
        actionList.add(true, "zoom", "Reset zoom", "Schema")
                .setKey("control R")
                .setIcon("zoom")
                .setListener(e -> schemaEditor.resetZoom());
        addBlockListButton(showBlockListAction);
    }

    private void addBlockListButton(Action action) {
        final JToggleButton toggleButton = new JToggleButton(action);
        toggleButton.setFocusable(false);
        toggleButton.setText("");
        blockListWindow.addComponentListener(new ComponentAction(ce -> {
            switch (ce.getID()) {
                case ComponentEvent.COMPONENT_SHOWN:
                    toggleButton.setSelected(true);
                    break;
                case ComponentEvent.COMPONENT_HIDDEN:
                    toggleButton.setSelected(false);
                    break;
            }
        }));
        toolBar.add(toggleButton);
        toolBar.addSeparator();
    }

    protected class SchemaEditorLayerUI extends LayerUI<SchemaEditor> {

        private JLabel tooltip = null;

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (tooltip != null) {
                final Rectangle bounds = tooltip.getBounds();
                if (bounds.getMaxX() > schemaEditor.getWidth()) {
                    tooltip.setLocation(schemaEditor.getWidth() - bounds.width, tooltip.getY());
                }
                if (bounds.getMaxY() > schemaEditor.getHeight()) {
                    tooltip.setLocation(tooltip.getX(), schemaEditor.getHeight() - bounds.height);
                }
                try {
                    g.translate(tooltip.getX(), tooltip.getY());
                    tooltip.print(g);
                } finally {
                    g.translate(-tooltip.getX(), -tooltip.getY());
                }
            }
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
            super.uninstallUI(c);
            ((JLayer) c).setLayerEventMask(0L);
        }

        @Override
        public void eventDispatched(AWTEvent e, JLayer<? extends SchemaEditor> l) {
            super.eventDispatched(e, l);
            switch (e.getID()) {
                case MouseEvent.MOUSE_ENTERED:
                    if (e.getSource() instanceof BlockComponent.Input) {
                        final BlockComponent.Input input = (BlockComponent.Input) e.getSource();
                        final Point point = ((MouseEvent) e).getLocationOnScreen();
                        SwingUtilities.convertPointFromScreen(point, schemaEditor);
                        showTooltip(point, input);
                    } else if (e.getSource() instanceof BlockComponent.Output) {
                        final BlockComponent.Output output = (BlockComponent.Output) e.getSource();
                        final Point point = ((MouseEvent) e).getLocationOnScreen();
                        SwingUtilities.convertPointFromScreen(point, schemaEditor);
                        showTooltip(point, output);
                    }
                    break;
                case MouseEvent.MOUSE_EXITED:
                    if (e.getSource() instanceof BlockComponent.Input || e.getSource() instanceof BlockComponent.Output) {
                        tooltip = null;
                        layer.repaint();
                    }
                    break;
            }
            if (e instanceof LinkShapeEvent) {
                switch (e.getID()) {
                    case LinkShapeEvent.MOUSE_ENTERED:
                        System.out.println(e);
                        break;
                    case LinkShapeEvent.MOUSE_EXITED:
                        System.out.println(e);
                        break;
                }
            }
        }

        public void showTooltip(Point point, BlockComponent.Input input) {
            showTooltip(String.format("%s: %s", input.getInput().getName(), input.getInput().getInputType().getCanonicalName()), point);
            layer.repaint();
        }

        public void showTooltip(Point point, BlockComponent.Output output) {
            showTooltip(String.format("%s: %s", output.getOutput().getName(), output.getOutput().getOutputType().getCanonicalName()), point);
            layer.repaint();
        }

        public void showTooltip(String text, Point point) {
            final JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setForeground(SystemColor.infoText);
            label.setBackground(SystemColor.info);
            label.setBorder(createCompoundBorder(createRaisedBevelBorder(), createEmptyBorder(3, 3, 3, 3)));
            label.setLocation(point.x + 10, point.y + 10);
            label.setSize(label.getPreferredSize());
            tooltip = label;
        }
    }
}
