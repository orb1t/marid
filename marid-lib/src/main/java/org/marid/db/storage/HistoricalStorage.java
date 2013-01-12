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

/**
 * Historical storage.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface HistoricalStorage extends Storage {

    /**
     * Checks whether the specific tag exists in the given timestamp.
     * @param ts A timestamp.
     * @param tag A tag.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exists(long ts, String... tag) throws IOException;

    /**
     * Checks whether the specific tags exist in the given timestamp.
     * @param ts A timestamp.
     * @param tags Tags.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exist(long ts, String[]... tags) throws IOException;

    /**
     * Checks whether the specific tag exists after the given timestamp.
     * @param ts A timestamp.
     * @param tag A tag.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existsAfter(long ts, String... tag) throws IOException;

    /**
     * Checks whether the specific tags exist after the given timestamp.
     * @param ts A timestamp.
     * @param tags Tags.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existAfter(long ts, String[]... tags) throws IOException;

    /**
     * Checks whether the specific tag exists before the given timestamp.
     * @param ts A timestamp.
     * @param tag A tag.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existsBefore(long ts, String... tag) throws IOException;

    /**
     * Checks whether the specific tags exist before the given timestamp.
     * @param ts A timestamp.
     * @param tags Tags.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean existBefore(long ts, String[]... tags) throws IOException;

    /**
     * Checks whether the specific tag exists between the given timestamps.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tag A tag.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exists(long ts, long tf, String... tag) throws IOException;

    /**
     * Checks whether the specific tags exist between the given timestamps.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tags Tags.
     * @return Check status.
     * @throws IOException An I/O exception.
     */
    public boolean exist(long ts, long tf, String[]... tags) throws IOException;

    /**
     * Deletes the specific tag at the given timestamp.
     * @param ts Timestamp.
     * @param tag A tag.
     * @return Deletion result.
     * @throws IOException An I/O exception.
     */
    public boolean delete(long ts, String... tag) throws IOException;

    /**
     * Deletes the specific tag at the given timestamp.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(long ts, String[]... tags) throws IOException;

    /**
     * Deletes the specific tag after the given timestamp.
     * @param ts Timestamp.
     * @param tag A tag.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteAfter(long ts, String... tag) throws IOException;

    /**
     * Deletes the specific tags after the given timestamp.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Deletetions count.
     * @throws IOException An I/O exception.
     */
    public long deleteAfter(long ts, String[]... tags) throws IOException;

    /**
     * Deletes the specific tags before the given timestamp.
     * @param ts Timestamp.
     * @param tag A tag.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteBefore(long ts, String... tag) throws IOException;

    /**
     * Deletes the specific tags before the given timestamp.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long deleteBefore(long ts, String[]... tags) throws IOException;

    /**
     * Deletes the specific tag within the given time bounds.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tag A tag.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(long ts, long tf, String... tag) throws IOException;

    /**
     * Deletes the specific tags within the given time bounds.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tags Tags.
     * @return Deletions count.
     * @throws IOException An I/O exception.
     */
    public long delete(long ts, long tf, String[]... tags) throws IOException;

    /**
     * Counts objects at the given timestamp by tag.
     * @param ts Timestamp.
     * @param tag A tag.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts, String... tag) throws IOException;

    /**
     * Counts objects at the given timestamp by tags.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts, String[]... tags) throws IOException;

    /**
     * Counts objects after the given timestamp.
     * @param ts Timestamp.
     * @param tag Tag.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countAfter(long ts, String... tag) throws IOException;

    /**
     * Counts objects after the given timestamp.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countAfter(long ts, String[]... tags) throws IOException;

    /**
     * Counts objects before the given timestamp.
     * @param ts Timestamp.
     * @param tag Tag.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countBefore(long ts, String... tag) throws IOException;

    /**
     * Counts objects before the given timestamp.
     * @param ts Timestamp.
     * @param tags Tags.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long countBefore(long ts, String[]... tags) throws IOException;

    /**
     * Counts objects within the given time bounds.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tag Tag.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts, long tf, String... tag) throws IOException;

    /**
     * Counts objects within the given time bounds.
     * @param ts Start timestamp.
     * @param tf Final timestamp.
     * @param tags Tags.
     * @return Objects count.
     * @throws IOException An I/O exception.
     */
    public long count(long ts, long tf, String[]... tags) throws IOException;

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
