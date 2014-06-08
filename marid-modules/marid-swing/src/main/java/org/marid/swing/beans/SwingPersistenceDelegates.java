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

package org.marid.swing.beans;

import org.marid.beans.ConstructorDelegate;
import org.marid.beans.MaridBeans;

import java.awt.*;
import java.beans.PersistenceDelegate;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingPersistenceDelegates implements MaridBeans {

    @Override
    public void visitPersistenceDelegates(BiConsumer<Class<?>, PersistenceDelegate> consumer) {
        consumer.accept(Point.class, new ConstructorDelegate<Point>((p, e) -> new Object[] {(int) p.getX(), (int) p.getY()}));
    }
}
