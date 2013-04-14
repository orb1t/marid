/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.bind

import groovy.util.logging.Log

import java.awt.Dimension
import java.awt.Point
import java.util.prefs.Preferences

/**
 * Swing specific methods.
 *
 * @author Dmitry Ovchinnikov 
 */
@Log
class SwingMethods {
    public static String hrf(Dimension dimension) {
        def w = dimension.width as int;
        def h = dimension.height as int;
        return "${w}x${h}";
    }

    public static String hrf(Point point) {
        def x = point.x as int;
        def y = point.y as int;
        return "${x},${y}";
    }

    public static void putDimension(Preferences prefs, String key, Dimension dimension) {
        prefs.put(key, hrf(dimension));
    }

    public static Dimension getDimension(Preferences prefs, String key, Dimension defValue) {
        def dim = prefs.get(key, hrf(defValue));
        try {
            def dimp = dim.split("x").collect {it.trim()};
            return new Dimension(dimp[0] as int, dimp[1] as int);
        } catch (x) {
            log.warning("Invalid dimension: {0}", x, dim);
            return defValue;
        }
    }

    public static void putPoint(Preferences prefs, String key, Point point) {
        prefs.put(key, hrf(point));
    }

    public static Point getPoint(Preferences prefs, String key, Point defValue) {
        def pnt = prefs.get(key, hrf(defValue));
        try {
            def pntp = pnt.split(",").collect {it.trim()};
            return new Point(pntp[0] as int, pntp[1] as int);
        } catch (x) {
            log.warning("Invalid point: {0}", x, pnt);
            return defValue;
        }
    }
}
