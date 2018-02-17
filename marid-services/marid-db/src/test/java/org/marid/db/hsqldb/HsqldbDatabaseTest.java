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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.marid.db.dao.NumericWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.*;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.stream.Collectors.toSet;
import static org.marid.test.TestGroups.SLOW;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Dmitry Ovchinnikov.
 */
@ContextConfiguration(classes = {HsqldbDatabaseTestConf.class})
public class HsqldbDatabaseTest extends AbstractTestNGSpringContextTests {

  private final long from = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
  private final long to = Instant.parse("2000-01-01T00:10:00Z").toEpochMilli();

  @Autowired
  private NumericWriter numericWriter;

  @Test(groups = {SLOW})
  public void test01() {
    final List<DataRecord<Double>> expected = new ArrayList<>();
    for (long t = from / 1000L; t < to / 1000L; t += 10L) {
      final Instant instant = Instant.ofEpochSecond(t);
      final DataRecord<Double> record = new DataRecord<>(0, instant.toEpochMilli(), 3.3);
      expected.add(record);
    }
    final Set<DataRecordKey> insertResult = numericWriter.merge(expected, true);
    assertEquals(
        insertResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()),
        expected.stream().map(DataRecord::getTimestamp).collect(toSet())
    );
    final List<DataRecord<Double>> actual = numericWriter.fetchRecords(new long[]{0L}, from, to);
    assertEquals(actual, expected);
    final long max = expected.stream().mapToLong(DataRecord::getTimestamp).max().orElse(0L);
    final List<DataRecord<Double>> actualMinus1 = numericWriter.fetchRecords(new long[]{0L}, from, max);
    assertEquals(actualMinus1.size(), expected.size() - 1);
  }

  @Test(groups = {SLOW})
  public void test02() {
    final long t1 = Instant.parse("2000-01-01T00:00:10Z").toEpochMilli();
    final long t2 = Instant.parse("2000-01-01T00:00:40Z").toEpochMilli();
    final long t3 = Instant.parse("2000-01-01T00:00:50Z").toEpochMilli();
    final List<DataRecord<Double>> records = ImmutableList.of(
        new DataRecord<>(0, t1, 2.3),
        new DataRecord<>(0, t2, 3.4),
        new DataRecord<>(0, t3, 3.3));
    final Set<DataRecordKey> mergeResult = numericWriter.merge(records, false);
    assertEquals(mergeResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()), ImmutableSet.of(t1, t2));
    assertEquals(numericWriter.fetchRecord(0, t1).getValue(), 2.3, 1e-3);
    assertEquals(numericWriter.fetchRecord(0, t2).getValue(), 3.4, 1e-3);
    assertEquals(numericWriter.fetchRecord(0, t3).getValue(), 3.3, 1e-3);
  }

  @Test(groups = {SLOW})
  public void test03() {
    final long tf = Instant.parse("2000-01-01T00:00:10Z").toEpochMilli();
    final long tt = Instant.parse("2000-01-01T00:00:40Z").toEpochMilli();
    assertEquals(3L, numericWriter.delete(tf, tt));
  }

  @Test(groups = {SLOW})
  public void test04() {
    assertEquals(57L, numericWriter.getRecordCount());
  }

  @Test(groups = {SLOW})
  public void test05() {
    final Map<Long, String> hashBefore = numericWriter.hash(from, to, false, "MD5");
    final long t = Instant.parse("2000-01-01T00:00:50Z").toEpochMilli();
    assertEquals(1L, numericWriter.delete(t, t + 1000L));
    final Map<Long, String> hashAfter = numericWriter.hash(from, to, false, "MD5");
    assertNotEquals(hashBefore, hashAfter);
  }

  @Test(groups = {SLOW})
  public void test06() {
    final List<DataRecord<Double>> expected = new ArrayList<>();
    for (long t = from / 1000L; t < to / 1000L; t += 10L) {
      final Instant instant = Instant.ofEpochSecond(t);
      final DataRecord<Double> record = new DataRecord<>(1, instant.toEpochMilli(), 3.3);
      expected.add(record);
    }
    final Set<DataRecordKey> insertResult = numericWriter.merge(expected, true);
    assertEquals(
        expected.stream().map(DataRecord::getTimestamp).collect(toSet()),
        insertResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()));
    final List<DataRecord<Double>> actual = numericWriter.fetchRecords(new long[]{1L}, from, to);
    assertEquals(actual, expected);
    assertEquals(numericWriter.tags(from, to), new long[]{0L, 1L});
    assertEquals(numericWriter.tagCount(from, to), 2);
  }

  @Test(groups = {SLOW})
  public void test07() {
    assertEquals(numericWriter.delete(from, from + DAYS.toMillis(1L)), 56L + 60L);
    assertEquals(numericWriter.fetchRecords(new long[]{0L, 1L}, from, to), Collections.emptyList());
    assertEquals(numericWriter.getRecordCount(), 0L);
  }
}
