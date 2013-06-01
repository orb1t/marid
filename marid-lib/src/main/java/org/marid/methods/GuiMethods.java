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

package org.marid.methods;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class GuiMethods {

    public static String hrf(Dimension dimension) {
        int w = dimension.width;
        int h = dimension.height;
        return w + "x" + h;
    }

    public static String hrf(Point point) {
        int x = point.x;
        int y = point.y;
        return x + "," + y;
    }

    public static void putDimension(Preferences prefs, String key, Dimension dimension) {
        prefs.put(key, hrf(dimension));
    }

    public static Dimension getDimension(Preferences prefs, String key, Dimension defValue) {
        String dim = prefs.get(key, hrf(defValue));
        try {
            String[] dimp = dim.split("x");
            return new Dimension(Integer.parseInt(dimp[0]), Integer.parseInt(dimp[1]));
        } catch (Exception x) {
            throw new IllegalArgumentException(dim, x);
        }
    }

    public static void putPoint(Preferences prefs, String key, Point point) {
        prefs.put(key, hrf(point));
    }

    public static Point getPoint(Preferences prefs, String key, Point defValue) {
        String pnt = prefs.get(key, hrf(defValue));
        try {
            String[] pntp = pnt.split(",");
            return new Point(Integer.parseInt(pntp[0]), Integer.parseInt(pntp[1]));
        } catch (Exception x) {
            throw new IllegalArgumentException(pnt, x);
        }
    }

    public static Object getAt(Action action, String key) {
        return action.getValue(key);
    }

    public static void putAt(Action action, String key, Object value) {
        action.putValue(key, value);
    }
}
