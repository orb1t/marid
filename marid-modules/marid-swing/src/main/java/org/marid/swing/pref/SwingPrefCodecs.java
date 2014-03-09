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

package org.marid.swing.pref;

import org.marid.pref.PrefCodecs;
import org.marid.pref.PrefReader;
import org.marid.pref.PrefWriter;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingPrefCodecs extends PrefCodecs {
    @Override
    public Map<Class<?>, PrefReader<?>> readers() {
        final Map<Class<?>, PrefReader<?>> m = new IdentityHashMap<>();
        m.put(Dimension.class, stringReader(s -> {
            final String[] parts = s.split("x");
            return new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }));
        m.put(Point.class, stringReader(s -> {
            final String[] parts = s.split(",");
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }));
        return m;
    }

    @Override
    public Map<Class<?>, PrefWriter<?>> writers() {
        final Map<Class<?>, PrefWriter<?>> m = new IdentityHashMap<>();
        m.put(Dimension.class, (PrefWriter<Dimension>) (p, k, v) -> p.put(k, v.width + "x" + v.height));
        m.put(Point.class, (PrefWriter<Point>) (p, k, v) -> p.put(k, v.x + "," + v.y));
        return m;
    }
}
