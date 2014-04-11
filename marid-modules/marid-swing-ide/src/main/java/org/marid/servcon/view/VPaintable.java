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

package org.marid.servcon.view;

import org.marid.servcon.model.Block;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class VPaintable {

    protected final BlockEditor blockEditor;
    protected final Block block;
    protected Point location;

    protected VPaintable(BlockEditor blockEditor, Block block, Point location) {
        this.blockEditor = blockEditor;
        this.block = block;
        this.location = location;
    }

    public abstract Dimension getSize();

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public final Rectangle getBounds() {
        return new Rectangle(getLocation(), getSize());
    }

    protected abstract List<? extends Region> getRegions();

    protected void onMouse(MouseEvent e) {
        for (final Region region : getRegions()) {
            final Rectangle bounds = region.getBounds();
            if (bounds.contains(e.getPoint())) {
                region.onMouse(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                        e.getX() - bounds.x, e.getY() - bounds.y,
                        e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
            }
        }
    }

    protected void paint(Graphics2D g) {
        for (final Region region : getRegions()) {
            final Point p = region.getLocation();
            try {
                g.translate(p.x, p.y);
                region.paint(g);
            } finally {
                g.translate(-p.x, -p.y);
            }
        }
    }

    protected abstract class Region {

        protected abstract Dimension getSize();

        protected abstract Point getLocation();

        protected final Rectangle getBounds() {
            return new Rectangle(getLocation(), getSize());
        }

        protected void paint(Graphics2D g) {
        }

        protected void onMouse(MouseEvent mouseEvent) {
        }
    }
}
