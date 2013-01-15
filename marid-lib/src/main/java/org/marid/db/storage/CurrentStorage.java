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
package org.marid.db.storage;

import java.io.IOException;
import java.util.List;
import javax.naming.Name;

/**
 * Data current storage.
 *
 * @auhor Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface CurrentStorage extends Storage {
    /**
     * Get a record by name.
     *
     * @param tag Tag.
     * @return Record.
     * @throws IOException An I/O exception.
     */
    public List get(Name tag) throws IOException;

    /**
     * Get records by names.
     *
     * @param tags Tags.
     * @return Records.
     * @throws IOException An I/O exception.
     */
    public List<List> get(List<Name> tags) throws IOException;

    /**
     * Get all the records.
     *
     * @return Records.
     * @throws IOException An I/O exception.
     */
    public List<List> get() throws IOException;
}
