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

package org.marid.service.data;

import java.io.Serializable;

/**
 * @author Dmitry Ovchinnikov
 */
public class DynResponse<T extends Serializable> extends Response {

    private static final long serialVersionUID = -2190258471309407326L;
    private final T data;

    public DynResponse(int code, String error, Object... args) {
        super(code, error, args);
        this.data = null;
    }

    public DynResponse(int code) {
        this(code, null);
    }

    public DynResponse(int code, T data) {
        super(code);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
