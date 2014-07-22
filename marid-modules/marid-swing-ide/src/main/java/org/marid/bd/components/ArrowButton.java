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

package org.marid.bd.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.SystemColor.controlDkShadow;

/**
 * @author Dmitry Ovchinnikov
 */
public class ArrowButton extends JToggleButton {

    public static final int ARROW_SIZE = 10;

    protected final int arrowPosition;

    public ArrowButton(String name, int arrowPosition) {
        this.arrowPosition = arrowPosition;
        setName(name);
        setUI(new BasicToggleButtonUI());
        setOpaque(false);
        setRolloverEnabled(true);
    }

    protected Color getArrowColor() {
        final ButtonModel m = getModel();
        return m.isRollover() ? RED : m.isPressed() ? GREEN : m.isSelected() ? RED.darker() : controlDkShadow;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(controlDkShadow);
        switch (arrowPosition) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                g.drawLine(0, ARROW_SIZE / 2, ARROW_SIZE / 2, ARROW_SIZE / 2);
                g.translate(ARROW_SIZE / 2, 0);
                g.setColor(getArrowColor());
                g.fillPolygon(new int[]{0, 0, ARROW_SIZE + 1}, new int[]{0, ARROW_SIZE, ARROW_SIZE / 2}, 3);
                g.setColor(controlDkShadow);
                g.translate(ARROW_SIZE, 0);
                g.drawLine(0, ARROW_SIZE / 2, ARROW_SIZE / 2, ARROW_SIZE / 2);
                break;
            case SwingConstants.SOUTH:
            case SwingConstants.NORTH:
                g.drawLine(ARROW_SIZE / 2, 0, ARROW_SIZE / 2, ARROW_SIZE / 2);
                g.translate(0, ARROW_SIZE / 2);
                g.setColor(getArrowColor());
                g.fillPolygon(new int[]{0, ARROW_SIZE, ARROW_SIZE / 2}, new int[]{0, 0, ARROW_SIZE + 1}, 3);
                g.setColor(controlDkShadow);
                g.translate(0, ARROW_SIZE);
                g.drawLine(ARROW_SIZE / 2, 0, ARROW_SIZE / 2, ARROW_SIZE / 2);
                break;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        switch (arrowPosition) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                return new Dimension(ARROW_SIZE * 2, ARROW_SIZE);
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                return new Dimension(ARROW_SIZE, 2 * ARROW_SIZE);
            default:
                throw new IllegalArgumentException("Invalid arrow position: " + arrowPosition);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
