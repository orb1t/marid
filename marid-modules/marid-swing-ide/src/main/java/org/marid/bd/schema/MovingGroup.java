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

import org.marid.swing.geom.ShapeUtils;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class MovingGroup {

    private final Map<Component, Point> componentMap = new IdentityHashMap<>();
    private Point point;

    public boolean isEmpty() {
        return componentMap.isEmpty();
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public void addComponent(Component component) {
        componentMap.put(component, component.getLocation());
    }

    public void prepareMove(Component component, Point point) {
        addComponent(component);
        this.point = point;
    }

    public void reset() {
        point = null;
        componentMap.clear();
    }
    
    public void move(Point point) {
        componentMap.forEach((c, p) -> c.setLocation(ShapeUtils.ptAdd(1, p, 1, point, -1, this.point)));
    }
}
