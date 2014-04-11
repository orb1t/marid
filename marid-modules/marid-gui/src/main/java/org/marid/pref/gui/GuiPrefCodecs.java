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

package org.marid.pref.gui;

import javafx.scene.paint.Color;
import org.marid.pref.PrefCodecs;
import org.marid.pref.PrefReader;
import org.marid.pref.PrefWriter;

import java.util.Map;

import static java.lang.Double.parseDouble;

/**
 * @author Dmitry Ovchinnikov.
 */
public class GuiPrefCodecs extends PrefCodecs {
    @Override
    public Map<Class<?>, PrefReader<?>> readers() {
        return new ReaderMapBuilder()
                .add(Color.class, splitReader(",",
                        a -> new Color(parseDouble(a[0]), parseDouble(a[1]), parseDouble(a[2]), parseDouble(a[3]))))
                .build();
    }

    @Override
    public Map<Class<?>, PrefWriter<?>> writers() {
        return new WriterMapBuilder()
                .add(Color.class, formattedWriter("%f,%f,%f,%f",
                        c -> new Double[] {c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity()}))
                .build();
    }
}
