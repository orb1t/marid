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

package org.marid.db.hsqldb;

import org.hsqldb.jdbc.JDBCPool;
import org.marid.db.dao.DaqWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;
import org.marid.misc.Digests;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;

/**
 * @author Dmitry Ovchinnikov.
 */
abstract class HsqldbDaqAbstractWriter<T extends Serializable> implements DaqWriter<T> {

    private final DataSource dataSource;
    private final String table;

    public HsqldbDaqAbstractWriter(DataSource dataSource, String table) {
        this.dataSource = dataSource;
        this.table = table;
    }

    @Override
    public long delete(Instant from, Instant to) {
        final String sql = "delete from " + table + " where TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            c.setAutoCommit(true);
            s.setTimestamp(1, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(2, new Timestamp(to.toEpochMilli()));
            return s.executeUpdate();
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public long delete(long[] tags, Instant from, Instant to) {
        final String sql = "delete from " + table + " where TAG in (unnest(?)) and TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            c.setAutoCommit(true);
            s.setObject(1, tags);
            s.setTimestamp(2, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(3, new Timestamp(to.toEpochMilli()));
            return s.executeUpdate();
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    void merge(Connection c, List<DataRecord<T>> dataRecords, String sql, Set<DataRecordKey> result) throws SQLException {
        try (final PreparedStatement s = c.prepareStatement(sql)) {
            for (final DataRecord<T> dataRecord : dataRecords) {
                s.setLong(1, dataRecord.getTag());
                s.setTimestamp(2, new Timestamp(dataRecord.getTimestamp().toEpochMilli()));
                setValue(s, 3, dataRecord.getValue());
                s.addBatch();
            }
            final int[] updates = s.executeBatch();
            for (int i = 0; i < updates.length; i++) {
                if (updates[i] > 0) {
                    result.add(dataRecords.get(i).getKey());
                }
            }
        }
    }

    @Override
    public Set<DataRecordKey> merge(List<DataRecord<T>> dataRecords, boolean insertOnly) {
        try (final Connection c = dataSource.getConnection()) {
            final Set<DataRecordKey> result = new LinkedHashSet<>();
            c.setAutoCommit(false);
            try {
                if (insertOnly) {
                    final String sql = "merge into " + table + " using (values(cast(? as varchar(256)), " +
                            "cast(? as timestamp), cast(? as " + getSqlTypeName() + "))) as VALS(TAG, TS, VAL) on " +
                            table + ".TAG = VALS.TAG and " + table + ".TS = VALS.TS " +
                            "when matched then update set " + table + ".VAL = VALS.VAL " +
                            "when not matched then insert values VALS.TAG, VALS.TS, VALS.VAL";
                    merge(c, dataRecords, sql, result);
                } else {
                    final String sqlu = "merge into " + table + " using (values(cast(? as varchar(256)), " +
                            "cast(? as timestamp), cast(? as " + getSqlTypeName() + "))) as VALS(TAG, TS, VAL) on " +
                            table + ".TAG = VALS.TAG and " + table + ".TS = VALS.TS and " +
                            table + ".VAL != VALS.VAL " +
                            "when matched then update set " + table + ".VAL = VALS.VAL";
                    merge(c, dataRecords, sqlu, result);
                    final String sqli = "merge into " + table + " using (values(cast(? as varchar(256)), " +
                            "cast(? as timestamp), cast(? as " + getSqlTypeName() + "))) as VALS(TAG, TS, VAL) on " +
                            table + ".TAG = VALS.TAG and " + table + ".TS = VALS.TS " +
                            "when not matched then insert values VALS.TAG, VALS.TS, VALS.VAL";
                    merge(c, dataRecords, sqli, result);
                }
                c.commit();
            } catch (Exception x) {
                c.rollback();
                throw x;
            }
            return result;
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public long[] tags(Instant from, Instant to) {
        final String sql = "select distinct TAG from " + table + " where TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setTimestamp(1, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(2, new Timestamp(to.toEpochMilli()));
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

    private Set<String> tags(String part, UnaryOperator<String> op) {
        final String sql = "select distinct TAG from " + table + " where TAG like ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, op.apply(part));
            final Set<String> set = new TreeSet<>();
            try (final ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString(1));
                }
            }
            return set;
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public long tagCount(Instant from, Instant to) {
        final String sql = "select count(distinct TAG) from " + table + " where TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setTimestamp(1, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(2, new Timestamp(to.toEpochMilli()));
            try (final ResultSet rs = s.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public DataRecord<T> fetchRecord(long tag, Instant instant) {
        final String sql = "select * from " + table + " where TAG = ? and TS = ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, tag);
            s.setTimestamp(2, new Timestamp(instant.toEpochMilli()));
            try (final ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    final Instant ts = Instant.ofEpochMilli(rs.getTimestamp(2).getTime());
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
    public List<DataRecord<T>> fetchRecords(long[] tags, Instant from, Instant to) {
        final String sql = "select * from " + table + " where TAG in (unnest(?)) and TS >= ? and TS < ?";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            final List<DataRecord<T>> result = new ArrayList<>();
            s.setObject(1, LongStream.of(tags).boxed().toArray(Long[]::new));
            s.setTimestamp(2, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(3, new Timestamp(to.toEpochMilli()));
            try (final ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    final long tag = rs.getLong(1);
                    final Instant ts = Instant.ofEpochMilli(rs.getTimestamp(2).getTime());
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
    public Map<Long, String> hash(Instant from, Instant to, boolean includeData, String algorithm) {
        final String sql = "select * from " + table + " where TS >= ? and TS < ? order by TAG, TS";
        try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
            final Map<Long, MessageDigest> digestMap = new TreeMap<>();
            s.setTimestamp(1, new Timestamp(from.toEpochMilli()));
            s.setTimestamp(2, new Timestamp(to.toEpochMilli()));
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
