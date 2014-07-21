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

package org.marid.bd.beans;

import org.marid.bd.multiplexors.Multiplexor;
import org.marid.beans.ConstructorDelegate;
import org.marid.beans.MaridBeans;

import java.beans.PersistenceDelegate;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class BdBeans implements MaridBeans {
    @Override
    public void visitPersistenceDelegates(BiConsumer<Class<?>, PersistenceDelegate> consumer) {
        consumer.accept(Multiplexor.class, new ConstructorDelegate<Multiplexor>(
                (m, e) -> new Object[] {m.getName(), m.getIconText(), m.getLabel(), m.getType(), m.getInputCount()}));
    }
}
