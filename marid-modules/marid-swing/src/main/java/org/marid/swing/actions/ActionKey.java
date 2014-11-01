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

package org.marid.swing.actions;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;

/**
 * @author Dmitry Ovchinnikov
 */
public class ActionKey extends CompositeName {

    public ActionKey(String name) {
        try {
            addAll(new CompositeName(name));
        } catch (InvalidNameException x) {
            throw new IllegalArgumentException(name, x);
        }
        if (size() < 2 || size() % 2 != 0) {
            throw new IllegalArgumentException("Name must have at least 2 elements and be even: " + toString());
        }
    }

    public String getGroup() {
        return get(size() - 2);
    }

    public String getLastName() {
        return get(size() - 1);
    }

    public String[] getPath() {
        final String[] path = new String[size() / 2 - 1];
        for (int i = 0; i < path.length; i++) {
            path[i] = get(i * 2 + 1);
        }
        return path;
    }
}
