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

package org.marid.services;

import java.beans.ConstructorProperties;
import java.io.Serializable;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class Request<T extends Response> implements Serializable {

    private static final long serialVersionUID = -4223372891746129864L;
    public final char command;

    @ConstructorProperties({"command"})
    public Request(char command) {
        this.command = command;
    }

    public abstract Class<T> getResponseClass();

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        Request that = (Request) obj;
        return command == that.command;
    }

    @Override
    public int hashCode() {
        return 31 + command;
    }
}
