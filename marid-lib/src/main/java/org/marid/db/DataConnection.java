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
import java.io.IOException;
import java.util.Set;
import org.marid.db.storage.Storage;

/**
 * Marid data connection.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface DataConnection extends Closeable {

    /**
     * Opens the connection.
     * @return The open flag.
     * @throws IOException An I/O exception.
     */
    public boolean open() throws IOException;

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
    public boolean isStorageSupported(Class<? extends Storage> type);

    /**
     * Get the supported storage type set.
     * @return Supported storage type set.
     */
    public Set<Class<? extends Storage>> getSupportedStorageTypeSet();

    /**
     * Get a storage by type.
     * @param <T> Storage type.
     * @param type Storage type class.
     * @return Storage or null if none was found.
     */
    public <T extends Storage> T getStorage(Class<T> type);
}
