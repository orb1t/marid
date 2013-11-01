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

package org.marid.func;

/**
 * @author Dmitry Ovchinnikov
 */
public class SkipException extends RuntimeException {

    private final Object[] args;

    public SkipException() {
        this(null, (Throwable) null);
    }

    public SkipException(String message, Object... args) {
        this(message, null, args);
    }

    public SkipException(Throwable cause) {
        this(null, cause);
    }

    public SkipException(String message, Throwable cause, Object... args) {
        super(message, cause, false, false);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
