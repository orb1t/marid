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

package org.marid.swing.geom;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ShapeUtils {

    public static boolean contains(Shape shape, double x, double y, double eps) {
        final double[] coords = new double[6];
        double cx = 0.0, cy = 0.0;
        for (final PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    cx = coords[0];
                    cy = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    final double dist = Line2D.ptSegDist(cx, cy, cx = coords[0], cy = coords[1], x, y);
                    if (dist <= eps) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public static boolean contains(Shape shape, Point2D point, double eps) {
        return contains(shape, point.getX(), point.getY(), eps);
    }

    public static Point add(Point... points) {
        final Point result = new Point();
        for (final Point point : points) {
            result.translate(point.x, point.y);
        }
        return result;
    }

    public static Point ptNeg(Point point) {
        return new Point(-point.x, -point.y);
    }

    public static Point ptAdd(int s1, Point p1, int s2, Point p2) {
        final Point point = new Point();
        point.translate(s1 * p1.x, s1 * p1.y);
        point.translate(s2 * p2.x, s2 * p2.y);
        return point;
    }

    public static Point ptAdd(int s1, Point p1, int s2, Point p2, int s3, Point p3) {
        final Point point = new Point();
        point.translate(s1 * p1.x, s1 * p1.y);
        point.translate(s2 * p2.x, s2 * p2.y);
        point.translate(s3 * p3.x, s3 * p3.y);
        return point;
    }

    public static MouseEvent mouseEvent(Component component, MouseEvent mouseEvent, int id, Point point) {
        return new MouseEvent(component,
                id, mouseEvent.getWhen(), mouseEvent.getModifiers(), point.x, point.y,
                mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen(),
                mouseEvent.getClickCount(), mouseEvent.isPopupTrigger(), mouseEvent.getButton());
    }
}
