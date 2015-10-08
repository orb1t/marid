/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import org.marid.swing.math.Geometry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * @author Dmitry Ovchinnikov
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 3)
@State(Scope.Benchmark)
public class DistanceBenchmark {

    private static final int COUNT = 10_000;
    private static final int[] X1 = IntStream.generate(() -> current().nextInt(10_000)).limit(COUNT).toArray();
    private static final int[] Y1 = IntStream.generate(() -> current().nextInt(10_000)).limit(COUNT).toArray();
    private static final int[] X2 = IntStream.generate(() -> current().nextInt(10_000)).limit(COUNT).toArray();
    private static final int[] Y2 = IntStream.generate(() -> current().nextInt(10_000)).limit(COUNT).toArray();

    private final Blackhole blackhole = new Blackhole();

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void computeDistanceFloat() {
        for (int i = 0; i < COUNT; i++) {
            blackhole.consume(Point.distance(X1[i], Y1[i], X2[i], Y2[i]));
        }
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void consumeDistanceInteger() {
        for (int i = 0; i < COUNT; i++) {
            blackhole.consume(Geometry.distance(X1[i], Y1[i], X2[i], Y2[i]));
        }
    }

    public static void main(String... args) throws Exception {
        new Runner(new OptionsBuilder().include(lookup().lookupClass().getSimpleName()).build()).run();
    }
}
