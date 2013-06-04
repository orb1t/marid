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

import static java.util.Objects.deepEquals;
import static java.util.Objects.hash;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class Response implements Serializable {

    public static final int STANDARD_RESPONSE = 0;
    public static final int UNSUPPORTED_REQUEST_RESPONSE = -1;
    public static final int BAD_REQUEST_RESPONSE = -2;
    public static final int UNSUPPORTED_VERSION_RESPONSE = -3;

    private static final long serialVersionUID = 3362213176969861901L;
    public final int code;
    public final String error;
    public final Object[] args;

    public Response(int code, String error, Object... args) {
        this.code = code;
        this.error = error;
        this.args = args;
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
            return code == o.code && Objects.equals(error, o.error) && deepEquals(args, o.args);
        }
    }

    @Override
    public int hashCode() {
        return hash(code, error, args);
    }
}
