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
package org.marid.db;

import java.io.Closeable;
import java.util.Set;

/**
 * Marid data connection.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface DataConnection extends Closeable {

    /**
     * Opens the connection.
     * @return The open flag.
     */
    public boolean open();

    /**
     * Checks whether the connection is opened.
     * @return Opened connection flag.
     */
    public boolean isOpened();

    /**
     * Checks whether the given storage type is supported.
     * @param type A storage type.
     * @return Check status.
     */
    public boolean isStorageSupported(Class<? extends DataStorage> type);

    /**
     * Get the supported storage type set.
     * @return Supported storage type set.
     */
    public Set<Class<? extends DataStorage>> getSupportedStorageTypeSet();

    /**
     * Get a storage by type.
     * @param <T> Storage type.
     * @param type Storage type class.
     * @return Storage or null if none was found.
     */
    public <T> T getStorage(Class<T> type);
}
