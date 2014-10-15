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

package org.marid.bd;

import org.marid.itf.Named;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface NamedBlock extends Named {

    Map<NamedBlock, String> NAMED_BLOCK_NAME_MAP = new WeakHashMap<>();

    default String getName() {
        return NAMED_BLOCK_NAME_MAP.get(this);
    }

    default void setName(String newName) {
        final Block block = (Block) this;
        block.fire(NamedBlockListener.class, this::getName, n -> NAMED_BLOCK_NAME_MAP.put(this, n), newName, NamedBlockListener::nameChanged);
    }
}
