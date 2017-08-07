/*-
 * #%L
 * marid-db
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

package org.marid.db.hsqldb;

import org.hsqldb.jdbc.JDBCPool;
import org.marid.db.dao.DaqReader;
import org.marid.db.data.DataRecord;
import org.marid.misc.Digests;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.stream.LongStream;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class HsqldbDaqAbstractReader<T extends Serializable> implements DaqReader<T> {

    final DataSource dataSource;
    final String table;

    protected HsqldbDaqAbstractReader(DataSource dataSource, String table) {
        this.dataSource = dataSource;
        this.table = table;
    }

    @Override
    public long[] tags(long from, long to) {
        final String sql = "select distinct TAG from " + table + " where TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setTimestamp(1, new Timestamp(from));
            s.setTimestamp(2, new Timestamp(to));
            final LongStream.Builder builder = LongStream.builder();
            try (final ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    builder.add(rs.getLong(1));
                }
            }
            return builder.build().toArray();
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public long tagCount(long from, long to) {
        final String sql = "select count(distinct TAG) from " + table + " where TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setTimestamp(1, new Timestamp(from));
            s.setTimestamp(2, new Timestamp(to));
            try (final ResultSet rs = s.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public DataRecord<T> fetchRecord(long tag, long instant) {
        final String sql = "select * from " + table + " where TAG = ? and TS = ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, tag);
            s.setTimestamp(2, new Timestamp(instant));
            try (final ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    final long ts = rs.getTimestamp(2).getTime();
                    final T value = getValue(rs, 3);
                    return new DataRecord<>(tag, ts, value);
                } else {
                    return null;
                }
            }
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public List<DataRecord<T>> fetchRecords(long[] tags, long from, long to) {
        final String sql = "select * from " + table + " where TAG in (unnest(?)) and TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            final List<DataRecord<T>> result = new ArrayList<>();
            s.setObject(1, LongStream.of(tags).boxed().toArray(Long[]::new));
            s.setTimestamp(2, new Timestamp(from));
            s.setTimestamp(3, new Timestamp(to));
            try (final ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    final long tag = rs.getLong(1);
                    final long ts = rs.getTimestamp(2).getTime();
                    final T value = getValue(rs, 3);
                    result.add(new DataRecord<>(tag, ts, value));
                }
            }
            return result;
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public Map<Long, String> hash(long from, long to, boolean includeData, String algorithm) {
        final String sql = "select * from " + table + " where TS >= ? and TS < ? order by TAG, TS";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            final Map<Long, MessageDigest> digestMap = new TreeMap<>();
            s.setTimestamp(1, new Timestamp(from));
            s.setTimestamp(2, new Timestamp(to));
            try (final ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    final Long tag = rs.getLong(1);
                    final Timestamp ts = rs.getTimestamp(2);
                    final MessageDigest digest = digestMap.computeIfAbsent(tag, t -> Digests.digest(algorithm));
                    digest.update(ByteBuffer.allocate(8).putLong(0, ts.getTime()));
                    if (includeData) {
                        digest.update(toByteArray(getValue(rs, 3)));
                    }
                }
            }
            final Map<Long, String> result = new TreeMap<>();
            digestMap.forEach((k, v) -> result.put(k, Base64.getEncoder().encodeToString(v.digest())));
            return result;
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public long getRecordCount() {
        try (final Connection c = dataSource.getConnection(); final Statement s = c.createStatement()) {
            try (final ResultSet rs = s.executeQuery("select count(*) from " + table)) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    protected abstract void setValue(PreparedStatement statement, int index, T value) throws SQLException;

    protected abstract T getValue(ResultSet resultSet, int index) throws SQLException;

    protected abstract byte[] toByteArray(@Nonnull T value);

    protected abstract String getSqlTypeName();

    @Override
    public void close() throws Exception {
        if (dataSource instanceof JDBCPool) {
            ((JDBCPool) dataSource).close(0);
        }
    }
}
