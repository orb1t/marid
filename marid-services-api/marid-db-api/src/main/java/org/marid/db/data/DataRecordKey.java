/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.db.data;

import org.jmlspecs.annotation.Immutable;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 */
@Immutable
public final class DataRecordKey {

    private final long tag;
    private final Instant timestamp;

    public DataRecordKey(long tag, @Nonnull Instant timestamp) {
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public long getTag() {
        return tag;
    }

    @Nonnull
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataRecordKey) {
            final DataRecordKey that = (DataRecordKey) obj;
            return tag == that.tag && timestamp.equals(that.timestamp);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s)", getClass().getSimpleName(), tag, timestamp);
    }
}
