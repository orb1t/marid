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

import org.marid.db.dao.DaqWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov.
 */
abstract class HsqldbDaqAbstractWriter<T extends Serializable> extends HsqldbDaqAbstractReader<T> implements DaqWriter<T> {

  public HsqldbDaqAbstractWriter(DataSource dataSource, String table) {
    super(dataSource, table);
  }

  @Override
  public long delete(long from, long to) {
    final String sql = "delete from " + table + " where TS >= ? and TS < ?";
    try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
      c.setAutoCommit(true);
      s.setTimestamp(1, new Timestamp(from));
      s.setTimestamp(2, new Timestamp(to));
      return s.executeUpdate();
    } catch (SQLException x) {
      throw new IllegalStateException(x);
    }
  }

  @Override
  public long delete(long[] tags, long from, long to) {
    final String sql = "delete from " + table + " where TAG in (unnest(?)) and TS >= ? and TS < ?";
    try (final Connection c = dataSource.getConnection(); final PreparedStatement s = c.prepareStatement(sql)) {
      c.setAutoCommit(true);
      s.setObject(1, tags);
      s.setTimestamp(2, new Timestamp(from));
      s.setTimestamp(3, new Timestamp(to));
      return s.executeUpdate();
    } catch (SQLException x) {
      throw new IllegalStateException(x);
    }
  }

  void merge(Connection c, List<DataRecord<T>> dataRecords, String sql, Set<DataRecordKey> result) throws SQLException {
    try (final PreparedStatement s = c.prepareStatement(sql)) {
      for (final DataRecord<T> dataRecord : dataRecords) {
        s.setLong(1, dataRecord.getTag());
        s.setTimestamp(2, new Timestamp(dataRecord.getTimestamp()));
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
}
