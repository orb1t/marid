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

package org.marid.collections;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ImmutableArraySet<E> extends AbstractSet<E> {

    public final List<E> list;

    @SafeVarargs
    public ImmutableArraySet(E... elements) {
        list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
    }

    ImmutableArraySet(ArrayList<E> elements) {
        list = elements;
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }
}
