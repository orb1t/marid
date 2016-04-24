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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.marid.db.dao.NumericWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

/**
 * @author Dmitry Ovchinnikov.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Category({NormalTests.class})
@ContextConfiguration(classes = {HsqldbDatabaseTestConf.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HsqldbDatabaseTest {

    private final Instant from = Instant.parse("2000-01-01T00:00:00Z");
    private final Instant to = Instant.parse("2000-01-01T00:10:00Z");

    @Autowired
    private NumericWriter numericWriter;

    @Test
    public void test_01_Insert() {
        final List<DataRecord<Double>> expected = new ArrayList<>();
        for (long t = from.getEpochSecond(); t < to.getEpochSecond(); t += 10L) {
            final Instant instant = Instant.ofEpochSecond(t);
            final DataRecord<Double> record = new DataRecord<>(0, instant, 3.3);
            expected.add(record);
        }
        final Set<DataRecordKey> insertResult = numericWriter.merge(expected, true);
        assertEquals(
                expected.stream().map(DataRecord::getTimestamp).collect(toSet()),
                insertResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()));
        final List<DataRecord<Double>> actual = numericWriter.fetchRecords(new long[] {0L}, from, to);
        assertEquals(actual, expected);
        final Instant max = expected.stream().map(DataRecord::getTimestamp).max(naturalOrder()).get();
        final List<DataRecord<Double>> actualMinus1 = numericWriter.fetchRecords(new long[] {0L}, from, max);
        assertEquals(expected.size() - 1, actualMinus1.size());
    }

    @Test
    public void test_02_Merge() {
        final Instant t1 = Instant.parse("2000-01-01T00:00:10Z");
        final Instant t2 = Instant.parse("2000-01-01T00:00:40Z");
        final Instant t3 = Instant.parse("2000-01-01T00:00:50Z");
        final List<DataRecord<Double>> records = ImmutableList.of(
                new DataRecord<>(0, t1, 2.3),
                new DataRecord<>(0, t2, 3.4),
                new DataRecord<>(0, t3, 3.3));
        final Set<DataRecordKey> mergeResult = numericWriter.merge(records, false);
        assertEquals(ImmutableSet.of(t1, t2), mergeResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()));
        assertEquals(2.3, numericWriter.fetchRecord(0, t1).getValue(), 1e-3);
        assertEquals(3.4, numericWriter.fetchRecord(0, t2).getValue(), 1e-3);
        assertEquals(3.3, numericWriter.fetchRecord(0, t3).getValue(), 1e-3);
    }

    @Test
    public void test_03_Delete() {
        final Instant tf = Instant.parse("2000-01-01T00:00:10Z");
        final Instant tt = Instant.parse("2000-01-01T00:00:40Z");
        assertEquals(3L, numericWriter.delete(tf, tt));
    }

    @Test
    public void test_04_RecordCount() {
        assertEquals(57L, numericWriter.getRecordCount());
    }

    @Test
    public void test_05_HashCodesNotIncludingData() {
        final Map<Long, String> hashBefore = numericWriter.hash(from, to, false, "MD5");
        final Instant t = Instant.parse("2000-01-01T00:00:50Z");
        assertEquals(1L, numericWriter.delete(t, t.plusSeconds(1L)));
        final Map<Long, String> hashAfter = numericWriter.hash(from, to, false, "MD5");
        assertNotEquals(hashBefore, hashAfter);
    }

    @Test
    public void test_06_Tags() {
        final List<DataRecord<Double>> expected = new ArrayList<>();
        for (long t = from.getEpochSecond(); t < to.getEpochSecond(); t += 10L) {
            final Instant instant = Instant.ofEpochSecond(t);
            final DataRecord<Double> record = new DataRecord<>(1, instant, 3.3);
            expected.add(record);
        }
        final Set<DataRecordKey> insertResult = numericWriter.merge(expected, true);
        assertEquals(
                expected.stream().map(DataRecord::getTimestamp).collect(toSet()),
                insertResult.stream().map(DataRecordKey::getTimestamp).collect(toSet()));
        final List<DataRecord<Double>> actual = numericWriter.fetchRecords(new long[] {1L}, from, to);
        assertEquals(actual, expected);
        assertArrayEquals(new long[] {0L, 1L}, numericWriter.tags(from, to));
        assertEquals(2, numericWriter.tagCount(from, to));
    }

    @Test
    public void test_07_DeleteAll() {
        assertEquals(56L + 60L, numericWriter.delete(from, from.plus(1L, ChronoUnit.DAYS)));
        assertEquals(Collections.emptyList(), numericWriter.fetchRecords(new long[] {0L, 1L}, from, to));
        assertEquals(0L, numericWriter.getRecordCount());
    }
}
