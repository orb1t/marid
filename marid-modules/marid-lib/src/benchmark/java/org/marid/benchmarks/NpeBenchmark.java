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

package org.marid.benchmarks;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
public class NpeBenchmark extends AbstractBenchmark {

    private static final int MAX_ITEMS = 10000;
    private static final int WARMUP_ROUNDS = 10000;
    private static final int BENCHMARK_ROUNDS = 500000;
    private static final Integer[] ARRAY = new Integer[MAX_ITEMS];
    private static long EXPECTED_SUM;

    @BeforeClass
    public static void fillArray() {
        long sum = 0L;
        for (int i = 0; i < MAX_ITEMS; i++) {
            ARRAY[i] = i;
            sum += i;
        }
        EXPECTED_SUM = sum;
    }

    @Test
    @BenchmarkOptions(warmupRounds = WARMUP_ROUNDS, benchmarkRounds = BENCHMARK_ROUNDS)
    public void testWithIfCheck() {
        long sum = 0L;
        for (int i = 0; i < MAX_ITEMS; i++) {
            Integer elem = ARRAY[i];
            if (elem != null) {
                sum += elem;
            }
        }
        assertEquals(EXPECTED_SUM, sum);
    }

    @Test
    @BenchmarkOptions(warmupRounds = WARMUP_ROUNDS, benchmarkRounds = BENCHMARK_ROUNDS)
    public void testWithNpeCatching() {
        long sum = 0L;
        for (int i = 0; i < MAX_ITEMS; i++) {
            try {
                sum += ARRAY[i];
            } catch (NullPointerException x) {
                // Ignore it
            }
        }
        assertEquals(EXPECTED_SUM, sum);
    }
}
