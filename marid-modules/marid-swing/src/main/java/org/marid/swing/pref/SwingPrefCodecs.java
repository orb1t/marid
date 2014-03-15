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
import java.util.Map;

import static java.lang.Integer.parseInt;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingPrefCodecs extends PrefCodecs {
    @Override
    public Map<Class<?>, PrefReader<?>> readers() {
        return new ReaderMapBuilder()
                .add(Dimension.class, splitReader("x", s -> new Dimension(parseInt(s[0]), parseInt(s[1]))))
                .add(Point.class, splitReader(",", s -> new Point(parseInt(s[0]), parseInt(s[1]))))
                .add(Rectangle.class, splitReader(",", s -> new Rectangle(parseInt(s[0]), parseInt(s[1]), parseInt(s[2]), parseInt(s[3]))))
                .build();
    }

    @Override
    public Map<Class<?>, PrefWriter<?>> writers() {
        return new WriterMapBuilder()
                .add(Dimension.class, (p, k, v) -> p.put(k, v.width + "x" + v.height))
                .add(Point.class, (p, k, v) -> p.put(k, v.x + "," + v.y))
                .add(Rectangle.class, (p, k, v) -> p.put(k, v.x + "," + v.y + "," + v.width + "," + v.height))
                .build();
    }
}
