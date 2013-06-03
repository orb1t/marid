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

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class Response implements Serializable {

    private static final long serialVersionUID = 3362213176969861901L;
    public final char code;
    public final String error;

    public Response(char code, String error) {
        this.code = code;
        this.error = error;
    }

    public Response(char code) {
        this(code, null);
    }

    public boolean hasError() {
        return error != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Response)) {
            return false;
        } else {
            Response o = (Response) obj;
            return code == o.code && Objects.equals(error, o.error);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, error);
    }
}
