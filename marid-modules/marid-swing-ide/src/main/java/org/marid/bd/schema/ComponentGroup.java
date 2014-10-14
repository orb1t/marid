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

import org.marid.swing.SwingUtil;
import org.marid.swing.geom.ShapeUtils;

import java.awt.*;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_MITER;

/**
 * @author Dmitry Ovchinnikov
 */
public class ComponentGroup {

    private final Map<Component, Point> componentMap = new IdentityHashMap<>();
    private Point point;
    private Rectangle selection;

    public boolean isEmpty() {
        return componentMap.isEmpty();
    }

    public void addComponent(Component component) {
        componentMap.put(component, component.getLocation());
    }

    public void reset() {
        point = null;
        selection = null;
        for (final Map.Entry<Component, Point> e : componentMap.entrySet()) {
            e.setValue(e.getKey().getLocation());
        }
    }

    public void clear() {
        point = null;
        componentMap.clear();
        selection = null;
    }

    public void move(Point point) {
        if (this.point == null) {
            this.point = new Point(point);
        }
        componentMap.forEach((c, p) -> c.setLocation(ShapeUtils.ptAdd(1, p, 1, point, -1, this.point)));
    }

    public Rectangle getBounds() {
        return componentMap.keySet()
                .stream()
                .map(Component::getBounds)
                .reduce((a, r) -> a.union(r))
                .orElseGet(Rectangle::new);
    }

    public boolean contains(Component component) {
        return componentMap.containsKey(component);
    }

    public boolean isEmptySelection() {
        return selection == null;
    }

    public void startSelection(Point point) {
        selection = new Rectangle(point);
    }

    public void updateSelection(Point point) {
        if (selection != null) {
            selection.width = point.x - selection.x;
            selection.height = point.y - selection.y;
        }
    }

    public void endSelection(Point point, Collection<Component> components) {
        if (selection == null) {
            reset();
            return;
        }
        updateSelection(point);
        final boolean lrSelection = selection.width < 0 || selection.height < 0;
        if (lrSelection) {
            if (selection.width < 0) {
                selection.x += selection.width;
                selection.width = Math.abs(selection.width);
            }
            if (selection.height < 0) {
                selection.y += selection.height;
                selection.height = Math.abs(selection.height);
            }
        }
        componentMap.clear();
        for (final Component component : components) {
            final Rectangle bounds = component.getBounds();
            if (lrSelection && bounds.intersects(selection) || !lrSelection && selection.contains(bounds)) {
                addComponent(component);
            }
        }
        reset();
    }

    public void paint(Graphics2D g) {
        if (selection != null) {
            final Rectangle r = new Rectangle(selection);
            final boolean lrSelection = r.width < 0 || r.height < 0;
            if (r.width < 0) {
                r.x += r.width;
                r.width = Math.abs(r.width);
            }
            if (r.height < 0) {
                r.y += r.height;
                r.height = Math.abs(r.height);
            }
            if (!r.isEmpty()) {
                final Stroke stroke = g.getStroke();
                final Color color = g.getColor();
                try {
                    if (lrSelection) {
                        g.setStroke(new BasicStroke(1.0f, CAP_BUTT, JOIN_MITER, 5.0f, new float[]{5.0f}, 0f));
                    }
                    g.setColor(SwingUtil.color(SystemColor.activeCaption, 70));
                    g.draw(r);
                } finally {
                    g.setStroke(stroke);
                    g.setColor(color);
                }
            }
        }
        if (componentMap.isEmpty()) {
            return;
        }
        final Color color = g.getColor();
        try {
            for (final Component component : componentMap.keySet()) {
                final Rectangle bounds = component.getBounds();
                bounds.grow(5, 5);
                g.setColor(SwingUtil.color(SystemColor.activeCaption, 30));
                g.fill(bounds);
                g.setColor(SystemColor.activeCaption);
                g.draw(bounds);
            }
        } finally {
            g.setColor(color);
        }
    }
}
