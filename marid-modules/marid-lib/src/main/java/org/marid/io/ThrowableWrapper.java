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

package org.marid.io;

/**
 * @author Dmitry Ovchinnikov
 */
public class ThrowableWrapper extends RuntimeException {

    private final String type;

    public ThrowableWrapper(String type, String message, ThrowableWrapper cause) {
        super(message, cause);
        this.type = type;
    }

    public ThrowableWrapper(Throwable e) {
        this(e.getClass().getName(), e.getMessage(), e.getCause() != null ? new ThrowableWrapper(e.getCause()) : null);
        setStackTrace(e.getStackTrace());
        if (e.getSuppressed().length > 0) {
            for (final Throwable supressed : e.getSuppressed()) {
                addSuppressed(new ThrowableWrapper(supressed));
            }
        }
    }

    public String getType() {
        return type;
    }

    @Override
    public synchronized ThrowableWrapper getCause() {
        return (ThrowableWrapper) super.getCause();
    }

    public Class<?> getTypeClass() {
        try {
            return Class.forName(getType());
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String toString() {
        return getMessage() != null ? type + " : " + getMessage() : type;
    }
}
