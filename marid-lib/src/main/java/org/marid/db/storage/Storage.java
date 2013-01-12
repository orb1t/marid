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
import java.util.BitSet;
import java.util.List;

/**
 * Data storage interface.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Storage {
    /**
     * Get all tags within the given root.
     * @param root Tag root.
     * @return Tag list.
     * @throws I/O exception.
     */
    public List<String[]> getTags(String... root) throws IOException;

    /**
     * Get tag existence status.
     * @param tag Tag.
     * @return Status.
     * @throws IOException An I/O exception.
     */
    public boolean exists(String... tag) throws IOException;

    /**
     * Checks whether all the tags exist.
     * @param tags Tag array.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exist(String[]... tags) throws IOException;

    /**
     * Checks whether the current user has read access to the specific tag.
     * @param tag A tag.
     * @return Access status.
     */
    public boolean hasReadAccess(String... tag);

    /**
     * Checks whether the current user has write access to a specific tag.
     * @param tag A tag.
     * @return Access status.
     */
    public boolean hasWriteAccess(String... tag);

    /**
     * Checks whether the current user has purge access.
     * @return Access status.
     */
    public boolean hasPurgeAccess();

    /**
     * Get the access code by tag.
     * @param tag A tag.
     * @return Access code.
     */
    public BitSet getAccessCode(String... tag);

    /**
     * Purge all the data in the current storage.
     * @throws IOException An I/O exception.
     */
    public void purge() throws IOException;

    /**
     * Deletes all the data by the specific tag.
     * @param tag A tag.
     * @return Deletion result.
     * @throws IOException An I/O exception.
     */
    public boolean delete(String... tag) throws IOException;

    /**
     * Deletes all the data by the specific tags.
     * @param tags Tags.
     * @throws IOException An I/O exception.
     */
    public void delete(String[]... tags) throws IOException;
}
