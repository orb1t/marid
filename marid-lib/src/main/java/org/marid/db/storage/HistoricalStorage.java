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
 * Historical storage.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface HistoricalStorage extends Storage {
    /**
     * Checks whether the specific tag exists in the given timestamp.
     * @param tag A tag.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exists(Name tag, long ts) throws IOException;

    /**
     * Checks whether the specific tags exist in the given timestamp.
     * @param tags Tags.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exist(List<Name> tags, long ts) throws IOException;

    /**
     * Checks whether the specific tag exists after the given timestamp.
     * @param tag A tag.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existsAfter(Name tag, long ts) throws IOException;

    /**
     * Checks whether the specific tags exist after the given timestamp.
     * @param tags Tags.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existAfter(List<Name> tags, long ts) throws IOException;

    /**
     * Checks whether the specific tag exists before the given timestamp.
     * @param tag A tag.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existsBefore(Name tag, long ts) throws IOException;

    /**
     * Checks whether the specific tags exist before the given timestamp.
     * @param tags Tags.
     * @param ts A timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existBefore(List<Name> tags, long ts) throws IOException;

    /**
     * Checks whether the specific tag exists between the given timestamps.
     * @param tag A tag.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exists(Name tag, long ts, long tf) throws IOException;

    /**
     * Checks whether the specific tags exist between the given timestamps.
     * @param tags Tags.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exist(List<Name> tags, long ts, long tf) throws IOException;

    /**
     * Deletes the specific tag at the given timestamp.
     * @param tag A tag.
     * @param ts Timestamp.
     * @return Deletion result.
     * @throws IOException An I/O exception.
     */
    public boolean delete(Name tag, long ts) throws IOException;

    /**
     * Deletes the specific tag at the given timestamp.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(List<Name> tags, long ts) throws IOException;

    /**
     * Deletes the specific tag after the given timestamp.
     * @param tag A tag.
     * @param ts Timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteAfter(Name tag, long ts) throws IOException;

    /**
     * Deletes the specific tags after the given timestamp.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Deletetions count.
     * @throws IOException An I/O exception.
     */
    public long deleteAfter(List<Name> tags, long ts) throws IOException;

    /**
     * Deletes the specific tags before the given timestamp.
     * @param tag A tag.
     * @param ts Timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteBefore(Name tag, long ts) throws IOException;

    /**
     * Deletes the specific tags before the given timestamp.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteBefore(List<Name> tags, long ts) throws IOException;

    /**
     * Deletes the specific tag within the given time bounds.
     * @param tag A tag.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(Name tag, long ts, long tf) throws IOException;

    /**
     * Deletes the specific tags within the given time bounds.
     * @param tags Tags.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(List<Name> tags, long ts, long tf) throws IOException;

    /**
     * Counts objects at the given timestamp by tag.
     * @param tag A tag.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(Name tag, long ts) throws IOException;

    /**
     * Counts objects at the given timestamp by tags.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(List<Name> tags, long ts) throws IOException;

    /**
     * Counts objects after the given timestamp.
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countAfter(Name tag, long ts) throws IOException;

    /**
     * Counts objects after the given timestamp.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countAfter(List<Name> tags, long ts) throws IOException;

    /**
     * Counts objects before the given timestamp.
     * @param tag Tag.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countBefore(Name tag, long ts) throws IOException;

    /**
     * Counts objects before the given timestamp.
     * @param tags Tags.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countBefore(List<Name> tags, long ts) throws IOException;

    /**
     * Counts objects within the given time bounds.
     * @param tag Tag.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(Name tag, long ts, long tf) throws IOException;

    /**
     * Counts objects within the given time bounds.
     * @param tags Tags.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(List<Name> tags, long ts, long tf) throws IOException;

    /**
     * Counts objects at the given timestamp.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts) throws IOException;

    /**
     * Counts objects after the given timestamp.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countAfter(long ts) throws IOException;

    /**
     * Counts objects before the given timestamp.
     * @param ts Timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countBefore(long ts) throws IOException;

    /**
     * Counts objects within the given time bounds.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts, long tf) throws IOException;
}
