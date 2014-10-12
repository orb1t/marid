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

package org.marid.bd;

import org.marid.bd.schema.SchemaEditor;
import org.marid.swing.MaridAction;
import org.marid.swing.actions.WindowAction;
import org.marid.swing.geom.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public interface BlockComponent {

    default JComponent getComponent() {
        return (JComponent) this;
    }

    Rectangle getBounds();

    Point getLocation();

    void setBounds(Rectangle rectangle);

    void setLocation(Point location);

    Dimension getPreferredSize();

    boolean isVisible();

    void validate();

    void setVisible(boolean visible);

    void update();

    default void remove() {
        final SchemaEditor schemaEditor = getSchemaEditor();
        schemaEditor.removeAllLinks(this);
        schemaEditor.remove(getComponent());
        schemaEditor.validate();
        schemaEditor.repaint();
    }

    default SchemaEditor getSchemaEditor() {
        return (SchemaEditor) getComponent().getParent();
    }

    default void updateBlock() {
        update();
        validate();
        setBounds(new Rectangle(getLocation(), getPreferredSize()));
    }

    default JPopupMenu popupMenu() {
        final JPopupMenu popupMenu = new JPopupMenu();
        if (getBlock() instanceof ConfigurableBlock) {
            final ConfigurableBlock b = (ConfigurableBlock) getBlock();
            popupMenu.add(new MaridAction("Settings", "settings", e -> {
                final Window window = b.createWindow(SwingUtilities.windowForComponent(getSchemaEditor()));
                window.addWindowListener(new WindowAction(we -> {
                    switch (we.getID()) {
                        case WindowEvent.WINDOW_CLOSED:
                            updateBlock();
                            getSchemaEditor().validate();
                            getSchemaEditor().repaint();
                            break;
                    }
                }));
                window.setVisible(true);
            }));
            popupMenu.addSeparator();
        }
        popupMenu.add(new MaridAction("Remove", "remove", e -> remove()));
        return popupMenu;
    }

    Block getBlock();

    default Input inputFor(String name) {
        return getInputs().stream().filter(i -> name.equals(i.getInput().getName())).findFirst().orElse(null);
    }

    default Output outputFor(String name) {
        return getOutputs().stream().filter(o -> name.equals(o.getOutput().getName())).findFirst().orElse(null);
    }

    List<Input> getInputs();

    List<Output> getOutputs();

    interface Port {

        BlockComponent getBlockComponent();

        default AbstractButton getButton() {
            return (AbstractButton) this;
        }

        Point getConnectionPoint();
    }

    interface Input extends Port {

        Block.Input getInput();

        @Override
        default Point getConnectionPoint() {
            final Rectangle bounds = ShapeUtils.toParent(getButton(), getBlockComponent().getComponent());
            final Rectangle blockBounds = getBlockComponent().getBounds();
            return new Point(blockBounds.x, blockBounds.y + bounds.y + bounds.height / 2);
        }
    }

    interface Output extends Port {

        Block.Output getOutput();

        @Override
        default Point getConnectionPoint() {
            final Rectangle bounds = ShapeUtils.toParent(getButton(), getBlockComponent().getComponent());
            final Rectangle blockBounds = getBlockComponent().getBounds();
            return new Point(blockBounds.x + blockBounds.width, blockBounds.y + bounds.y + bounds.height / 2);
        }
    }
}
