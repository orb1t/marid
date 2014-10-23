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

package org.marid.bd.shapes;

import org.marid.bd.BlockComponent;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;

import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class LinkShape {

    public static final Stroke NORMAL = new BasicStroke(1.0f);
    public static final Stroke VECTOR = new BasicStroke(1.0f, CAP_SQUARE, JOIN_MITER, 5.0f, new float[]{5.0f}, 0.0f);

    public final BlockComponent.Output output;
    public final BlockComponent.Input input;

    public LinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        this.output = output;
        this.input = input;
    }

    public abstract void update();

    public void paint(Graphics2D g) {
        g.draw(getShape());
    }

    public abstract Shape getShape();

    public boolean isAssociatedWith(BlockComponent blockComponent) {
        return output.getBlockComponent() == blockComponent || input.getBlockComponent() == blockComponent;
    }

    public JPopupMenu popupMenu() {
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new MaridAction("Remove", "remove",
                ev -> output.getBlockComponent().getSchemaEditor().removeLink(this)));
        return popupMenu;
    }

    public boolean isValid() {
        final Class<?> it = input.getInput().getInputType();
        final Class<?> ot = output.getOutput().getOutputType();
        return it.isAssignableFrom(ot) || it.isArray() && it.getComponentType().isAssignableFrom(ot);
    }

    public Color getColor() {
        if (isValid()) {
            return SystemColor.controlDkShadow;
        } else {
            return SystemColor.RED;
        }
    }

    public Stroke getStroke() {
        return getOutputType().isArray() ? VECTOR : NORMAL;
    }

    public Class<?> getOutputType() {
        return output.getOutput().getOutputType();
    }

    public Class<?> getInputType() {
        return input.getInput().getInputType();
    }

    @Override
    public int hashCode() {
        return output.hashCode() ^ input.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LinkShape) {
            final LinkShape that = (LinkShape) obj;
            return this.input == that.input && this.output == that.output;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s - %s:%s",
                System.identityHashCode(output), output.getOutput().getName(),
                System.identityHashCode(input), input.getInput().getName());
    }
}
