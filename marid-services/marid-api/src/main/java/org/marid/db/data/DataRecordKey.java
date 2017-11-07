/*-
 * #%L
 * marid-api
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.db.data;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class DataRecordKey {

	private final long tag;
	private final long timestamp;

	public DataRecordKey(long tag, long timestamp) {
		this.tag = tag;
		this.timestamp = timestamp;
	}

	public long getTag() {
		return tag;
	}

	public long getTimestamp() {
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
			return tag == that.tag && timestamp == that.timestamp;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("%s(%s,%s)", getClass().getSimpleName(), tag, timestamp);
	}
}
